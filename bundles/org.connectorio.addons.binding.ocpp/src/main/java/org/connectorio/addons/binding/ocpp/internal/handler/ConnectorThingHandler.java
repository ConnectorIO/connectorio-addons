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
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import eu.chargetime.ocpp.model.core.ValueFormat;
import eu.chargetime.ocpp.model.core.ChargingProfile;
import eu.chargetime.ocpp.model.core.ChargingProfileKindType;
import eu.chargetime.ocpp.model.core.ChargingProfilePurposeType;
import eu.chargetime.ocpp.model.core.ChargingRateUnitType;
import eu.chargetime.ocpp.model.core.ChargingSchedule;
import eu.chargetime.ocpp.model.core.ChargingSchedulePeriod;
import eu.chargetime.ocpp.model.core.RemoteStartTransactionRequest;
import eu.chargetime.ocpp.model.core.RemoteStopTransactionRequest;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.measure.Quantity;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.connectorio.addons.binding.ocpp.internal.config.ChargerConfig;
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

public class ConnectorThingHandler extends GenericThingHandlerBase<ServerBridgeHandler, ChargerConfig> implements
  StatusNotificationHandler, TransactionHandler, MeterValuesHandler {

  private final AtomicInteger transactionId = new AtomicInteger();
  private final Logger logger = LoggerFactory.getLogger(ConnectorThingHandler.class);
  
  private OcppSender ocppSender;
  private String chargerSerialNumber;
  private Integer currentTransactionId;
  private String remoteStartTag;

  public ConnectorThingHandler(Thing thing) {
    super(thing);
  }

  protected void setOcppSender(OcppSender sender, String chargerSerial) {
    this.ocppSender = sender;
    this.chargerSerialNumber = chargerSerial;
  }

  @Override
  public void initialize() {
    Optional<ChargerConfig> config = getThingConfig();
    if (config.isPresent()) {
      remoteStartTag = config.get().remoteStartTag;
      if (remoteStartTag == null || remoteStartTag.trim().isEmpty()) {
        remoteStartTag = "openhab";
      }
    } else {
      remoteStartTag = "openhab";
    }
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    String channelId = channelUID.getId();
    if (OcppBindingConstants.CHARGE_LIMIT.getAsString().equals(channelId)) {
      handleChargeLimitCommand(command);
    } else if (OcppBindingConstants.CHARGING.getAsString().equals(channelId)) {
      handleChargingCommand(command);
    }
  }

  private void handleChargingCommand(Command command) {
    if (!(command instanceof OnOffType)) {
      logger.warn("Unsupported command type for charging control: {}", command.getClass());
      return;
    }

    if (OnOffType.ON.equals(command)) {
      sendRemoteStartTransaction();
    } else {
      sendRemoteStopTransaction();
    }
  }

  private void sendRemoteStartTransaction() {
    if (ocppSender == null || chargerSerialNumber == null) {
      logger.warn("OcppSender or charger serial not set. Cannot send RemoteStartTransaction.");
      return;
    }

    ChargerReference chargerRef = new ChargerReference(chargerSerialNumber);
    RemoteStartTransactionRequest request = new RemoteStartTransactionRequest(remoteStartTag);

    ocppSender.send(chargerRef, request).whenComplete((confirmation, throwable) -> {
      if (throwable != null) {
        logger.warn("Failed to send RemoteStartTransaction with tag {}", remoteStartTag, throwable);
      } else {
        logger.info("RemoteStartTransaction with tag {} sent successfully: {}", remoteStartTag, confirmation);
      }
    });
  }

  private void sendRemoteStopTransaction() {
    if (ocppSender == null || chargerSerialNumber == null) {
      logger.warn("OcppSender or charger serial not set. Cannot send RemoteStopTransaction.");
      return;
    }

    if (currentTransactionId == null) {
      logger.warn("No active transaction found for RemoteStopTransaction.");
      return;
    }

    ChargerReference chargerRef = new ChargerReference(chargerSerialNumber);
    RemoteStopTransactionRequest request = new RemoteStopTransactionRequest(currentTransactionId);

    ocppSender.send(chargerRef, request).whenComplete((confirmation, throwable) -> {
      if (throwable != null) {
        logger.warn("Failed to send RemoteStopTransaction for transaction {}", currentTransactionId, throwable);
      } else {
        logger.info("RemoteStopTransaction for transaction {} sent successfully: {}", currentTransactionId, confirmation);
      }
    });
  }

  private void handleChargeLimitCommand(Command command) {
    double limit;
    if (command instanceof DecimalType) {
      limit = ((DecimalType) command).doubleValue();
    } else if (command instanceof QuantityType) {
      limit = ((QuantityType<?>) command).doubleValue();
    } else {
      logger.warn("Unsupported command type for chargeLimit: {}", command.getClass());
      return;
    }

    sendChargingProfile(limit);
  }

  private void sendChargingProfile(double limit) {
    if (ocppSender == null || chargerSerialNumber == null) {
      logger.warn("OcppSender or charger serial not set. Cannot send charging profile.");
      return;
    }

    ChargerReference chargerRef = new ChargerReference(chargerSerialNumber);

    // Create charging profile with the specified limit
    ChargingSchedulePeriod period = new ChargingSchedulePeriod(0, limit);
    ChargingSchedule schedule = new ChargingSchedule(
      ChargingRateUnitType.A,
      new ChargingSchedulePeriod[]{ period }
    );

    ChargingProfile profile = new ChargingProfile();
    profile.setChargingProfileId(1);
    profile.setStackLevel(0);
    profile.setChargingProfilePurpose(ChargingProfilePurposeType.TxDefaultProfile);
    profile.setChargingProfileKind(ChargingProfileKindType.Relative);
    profile.setChargingSchedule(schedule);

    SetChargingProfileRequest setProfileRequest = new SetChargingProfileRequest(1, profile);
    
    ocppSender.send(chargerRef, setProfileRequest).whenComplete((confirmation, throwable) -> {
      if (throwable != null) {
        logger.warn("Failed to send SetChargingProfile with limit {}", limit, throwable);
      } else {
        logger.info("SetChargingProfile with limit {} sent successfully: {}", limit, confirmation);
      }
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
          UID ref = OcppMeasurementMapping.get(sample.getMeasurand());
          if (ref != null) {
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

    return new StatusNotificationConfirmation();
  }

  @Override
  public StartTransactionConfirmation handleStartTransaction(StartTransactionRequest request) {
    String tag = request.getIdTag();

    ThingHandlerCallback callback = getCallback();
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "idTag"), new StringType(tag));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CHARGING.getAsString()), OnOffType.ON);
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "timestampStart"), new DateTimeType(request.getTimestamp()));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "meterStart"), new QuantityType<>(request.getMeterStart(), Units.WATT_HOUR));

    IdTagInfo tagInfo = new IdTagInfo(AuthorizationStatus.Accepted);
    return new StartTransactionConfirmation(tagInfo, generateId());
  }

  @Override
  public StopTransactionConfirmation handleStopTransaction(StopTransactionRequest request) {
    String tag = request.getIdTag();

    Integer txId = request.getTransactionId();
    if (transactionId.get() != txId + 1) {
      return new StopTransactionConfirmation();
    }

    currentTransactionId = null;
    ThingHandlerCallback callback = getCallback();
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "idTag"), new StringType(tag));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), OcppBindingConstants.CHARGING.getAsString()), OnOffType.OFF);
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "timestampStop"), new DateTimeType(request.getTimestamp()));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "meterStop"), new QuantityType<>(request.getMeterStop(), Units.WATT_HOUR));

    return new StopTransactionConfirmation();
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
