package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.core.AuthorizationStatus;
import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.IdTagInfo;
import eu.chargetime.ocpp.model.core.MeterValue;
import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.SampledValue;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.ResetRequest;
import eu.chargetime.ocpp.model.core.ResetType;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import eu.chargetime.ocpp.model.core.UnlockConnectorRequest;
import eu.chargetime.ocpp.model.core.ValueFormat;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.measure.Quantity;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.connectorio.addons.binding.ocpp.internal.config.ConnectorConfig;
import org.connectorio.addons.binding.ocpp.internal.server.OcppMeasurementMapping;
import org.connectorio.addons.binding.ocpp.internal.server.listener.MeterValuesHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.StatusNotificationHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.TransactionHandler;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.units.indriya.quantity.Quantities;

public class ConnectorThingHandler extends GenericThingHandlerBase<ServerBridgeHandler, ConnectorConfig> implements
  StatusNotificationHandler, TransactionHandler, MeterValuesHandler, ConnectorCommandContext {

  // Package-scoped for testability
  void setTransactionId(int value) {
    this.transactionId.set(value);
  }

  private static final long DEFAULT_PROFILE_MIN_INTERVAL_MS = 500L;

  // Transaction-id sequence. Replaced with a charger-wide shared sequence (see setTransactionSequence)
  // so connectors on the same charge point never issue colliding ids — StopTransaction carries only a
  // transactionId, no connectorId, so colliding ids cannot be routed back to the right connector.
  private AtomicInteger transactionId = new AtomicInteger(1);
  private final Logger logger = LoggerFactory.getLogger(ConnectorThingHandler.class);

  private OcppSender ocppSender;
  private String chargerSerialNumber;
  private Integer currentTransactionId;
  private String remoteStartTag;
  private Integer connectorId;

  private final ChargeLimitCommandHandler chargeLimitHandler;
  private final ChargingCommandHandler chargingHandler;

  public ConnectorThingHandler(Thing thing) {
    this(thing, new ChargeLimitCommandHandler(), new ChargingCommandHandler());
  }

  ConnectorThingHandler(Thing thing, ChargeLimitCommandHandler chargeLimitHandler, ChargingCommandHandler chargingHandler) {
    super(thing);
    this.chargeLimitHandler = chargeLimitHandler;
    this.chargingHandler = chargingHandler;
  }

  protected void setOcppSender(OcppSender sender, String chargerSerial) {
    this.ocppSender = sender;
    this.chargerSerialNumber = chargerSerial;
  }

  /**
   * Share one transaction-id sequence across all connectors of a charge point so the ids stay unique
   * per charger. Called by {@link ChargerConnectorAdapter} when the connector is registered.
   */
  void setTransactionSequence(AtomicInteger sequence) {
    this.transactionId = sequence;
  }

  @Override
  public OcppSender getOcppSender() {
    return ocppSender;
  }

  @Override
  public String getChargerSerialNumber() {
    return chargerSerialNumber;
  }

  @Override
  public String getRemoteStartTag() {
    return remoteStartTag;
  }

  @Override
  public Integer getCurrentTransactionId() {
    return currentTransactionId;
  }

  @Override
  public Integer getConnectorId() {
    return connectorId;
  }

  @Override
  public java.util.concurrent.ScheduledExecutorService getScheduler() {
    return scheduler;
  }

  @Override
  public long getProfileMinIntervalMs() {
    return getThingConfig()
        .map(config -> config.profileMinIntervalMs)
        .filter(value -> value != null && value >= 0)
        .map(Integer::longValue)
        .orElse(DEFAULT_PROFILE_MIN_INTERVAL_MS);
  }

  @Override
  public void initialize() {
    Optional<ConnectorConfig> config = getThingConfig();
    if (config.isPresent()) {
      remoteStartTag = config.get().remoteStartTag;
      if (remoteStartTag == null || remoteStartTag.trim().isEmpty()) {
        remoteStartTag = ConnectorConfig.DEFAULT_REMOTE_START_TAG;
      }
      connectorId = config.get().connectorId;
    } else {
      remoteStartTag = ConnectorConfig.DEFAULT_REMOTE_START_TAG;
    }
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    String channelId = channelUID.getId();
    if (OcppBindingConstants.CHARGE_LIMIT.getAsString().equals(channelId)) {
      chargeLimitHandler.handle(command, this);
    } else if (OcppBindingConstants.CHARGING.getAsString().equals(channelId)) {
      chargingHandler.handle(command, this);
    } else if (OcppBindingConstants.RESET.getAsString().equals(channelId)) {
      if (command == OnOffType.ON) {
        sendReset();
      }
    } else if (OcppBindingConstants.LOCK.getAsString().equals(channelId)) {
      if (command == OnOffType.ON) {
        sendUnlock();
      }
    } else if (OcppBindingConstants.PAUSE.getAsString().equals(channelId)) {
      if (command instanceof OnOffType) {
        if (command == OnOffType.ON) {
          chargeLimitHandler.pause(this);
        } else {
          chargeLimitHandler.resume(this);
        }
      }
    }
  }

  private void sendReset() {
    if (ocppSender == null || chargerSerialNumber == null) {
      return;
    }
    ChargerReference reference = new ChargerReference(chargerSerialNumber);
    ocppSender.send(reference, new ResetRequest(ResetType.Soft)).whenComplete((confirmation, ex) -> {
      if (ex != null) {
        logger.warn("Reset(Soft) for {} failed: {}", getThing().getUID(), ex.getMessage());
      } else {
        logger.info("Reset(Soft) for {}: {}", getThing().getUID(), confirmation);
      }
      getCallback().stateUpdated(
          new ChannelUID(getThing().getUID(), OcppBindingConstants.RESET.getAsString()), OnOffType.OFF);
    });
  }

  private void sendUnlock() {
    if (ocppSender == null || chargerSerialNumber == null || connectorId == null) {
      return;
    }
    ChargerReference reference = new ChargerReference(chargerSerialNumber);
    ocppSender.send(reference, new UnlockConnectorRequest(connectorId)).whenComplete((confirmation, ex) -> {
      if (ex != null) {
        logger.warn("UnlockConnector for {} failed: {}", getThing().getUID(), ex.getMessage());
      } else {
        logger.info("UnlockConnector for {}: {}", getThing().getUID(), confirmation);
      }
      getCallback().stateUpdated(
          new ChannelUID(getThing().getUID(), OcppBindingConstants.LOCK.getAsString()), OnOffType.OFF);
    });
  }

  @Override
  public MeterValuesConfirmation handleMeterValues(MeterValuesRequest request) {
    ThingHandlerCallback callback = getCallback();

    // push transaction id
    Integer transactionId = request.getTransactionId();
    // transactionId is null outside of an active charge transaction
    if (transactionId != null) {
      currentTransactionId = transactionId;
      callback.stateUpdated(new ChannelUID(getThing().getUID(), "transactionId"), new DecimalType(transactionId));
    }

    for (MeterValue value : request.getMeterValue()) {
      ZonedDateTime timestamp = value.getTimestamp();
      // update timestamp for further channel updates
      callback.stateUpdated(new ChannelUID(getThing().getUID(), "timestamp"), new DateTimeType(timestamp));

      logger.debug("Received samples for transaction {}: {}", transactionId, value);

      for (SampledValue sample : value.getSampledValue()) {
        if (!ValueFormat.Raw.equals(sample.getFormat())) {
          // unsupported case with encrypted measurements
          continue;
        }

        try {
          Double measurement = Double.valueOf(sample.getValue());
          for (UID ref : OcppMeasurementMapping.channelsFor(sample)) {
            ChannelUID uid = new ChannelUID(getThing().getUID(), ref.getAsString());
            State state = parse(measurement, uid, sample);
            getCallback().stateUpdated(uid, state);
          }
        } catch (NumberFormatException e) {
          logger.debug("Could not parse value of measurement {}", sample, e);
        }
      }
    }

    return new MeterValuesConfirmation();
  }

  @Override
  public StatusNotificationConfirmation handleStatusNotification(StatusNotificationRequest request) {
    ChargePointStatus status = request.getStatus();

    StringType val = new StringType(status.name());
    getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "chargePointStatus"), val);

    if (status == ChargePointStatus.Available
        || status == ChargePointStatus.Finishing
        || status == ChargePointStatus.SuspendedEV
        || status == ChargePointStatus.SuspendedEVSE) {
      resetSessionState(null, null);
    }

    return new StatusNotificationConfirmation();
  }

  @Override
  public StartTransactionConfirmation handleStartTransaction(StartTransactionRequest request) {
    String tag = request.getIdTag();

    int txId = generateId();
    currentTransactionId = txId;

    ThingHandlerCallback callback = getCallback();
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "idTag"), new StringType(tag));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CHARGING.getAsString()), OnOffType.ON);
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "timestampStart"), new DateTimeType(request.getTimestamp()));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "meterStart"), new QuantityType<>(request.getMeterStart(), Units.WATT_HOUR));

    IdTagInfo tagInfo = new IdTagInfo(AuthorizationStatus.Accepted);
    return new StartTransactionConfirmation(tagInfo, txId);
  }

  @Override
  public StopTransactionConfirmation handleStopTransaction(StopTransactionRequest request) {
    String tag = request.getIdTag();

    Integer txId = request.getTransactionId();
    // Only act on a stop for this connector's own running transaction; with one charge point exposing
    // several connectors a StopTransaction can be dispatched here for another connector's id.
    if (currentTransactionId == null || !currentTransactionId.equals(txId)) {
      return new StopTransactionConfirmation();
    }

    resetSessionState(tag, OnOffType.OFF);
    // Set stop-specific fields
    ThingHandlerCallback callback = getCallback();
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "timestampStop"), new DateTimeType(request.getTimestamp()));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "meterStop"), new QuantityType<>(request.getMeterStop(), Units.WATT_HOUR));

    return new StopTransactionConfirmation();
  }
  /**
   * Resets all relevant session state and channels when a transaction ends or charger becomes available.
   * If any argument is null, that field is not updated (for use in StatusNotification events).
   */
  private void resetSessionState(String idTag, OnOffType chargingState) {
    currentTransactionId = null;
    ThingHandlerCallback callback = getCallback();
    if (idTag != null) {
      callback.stateUpdated(new ChannelUID(getThing().getUID(), "idTag"), new StringType(idTag));
    }
    if (chargingState != null) {
      callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CHARGING.getAsString()), chargingState);
    }
    // Always reset power/current channels
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.POWER_ACTIVE_IMPORT.getAsString()), new QuantityType<>(0, Units.WATT));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CURRENT_IMPORT.getAsString()), new QuantityType<>(0, Units.AMPERE));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CURRENT_IMPORT_L1.getAsString()), new QuantityType<>(0, Units.AMPERE));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CURRENT_IMPORT_L2.getAsString()), new QuantityType<>(0, Units.AMPERE));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CURRENT_IMPORT_L3.getAsString()), new QuantityType<>(0, Units.AMPERE));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CURRENT_OFFERED.getAsString()), new QuantityType<>(0, Units.AMPERE));
  }

  private static State parse(Double measurement, ChannelUID uid, SampledValue sample) {
    String unit = sample.getUnit();
    if (unit != null) {
      // Normalize unit names that don't match JSR-385 format
      switch (unit) {
        case "Celsius": unit = "°C"; break;
        case "Fahrenheit": unit = "°F"; break;
        default: break;
      }
      Quantity<?> quantity = Quantities.getQuantity("1 " + unit);
      return new QuantityType<>(measurement, quantity.getUnit());
    }

    // default assumed from specs, when unit is not specified it fall backs to "Wh"
    return new QuantityType<>(measurement, Units.WATT_HOUR);
  }

  private int generateId() {
    int transaction = transactionId.getAndIncrement();
    if (transaction == Integer.MAX_VALUE) {
      transactionId.set(0);
    }
    return transaction;
  }

}
