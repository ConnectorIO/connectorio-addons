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
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.ocpp.internal.config.ChargerConfig;
import org.connectorio.addons.binding.ocpp.internal.server.OcppMeasurementMapping;
import org.connectorio.addons.binding.ocpp.internal.server.listener.MeterValuesHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.StatusNotificationHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.TransactionHandler;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.quantity.Quantities;

public class ConnectorThingHandler extends GenericThingHandlerBase<ServerBridgeHandler, ChargerConfig> implements
  StatusNotificationHandler, TransactionHandler, MeterValuesHandler {

  private final AtomicInteger transactionId = new AtomicInteger();
  private final Logger logger = LoggerFactory.getLogger(ConnectorThingHandler.class);

  public ConnectorThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public MeterValuesConfirmation handleMeterValues(MeterValuesRequest request) {
    ThingHandlerCallback callback = getCallback();

    // push transaction id
    Integer transactionId = request.getTransactionId();
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "transactionId"), new DecimalType(transactionId));

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

    ThingHandlerCallback callback = getCallback();
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "idTag"), new StringType(tag));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "timestampStop"), new DateTimeType(request.getTimestamp()));
    callback.stateUpdated(new ChannelUID(getThing().getUID(), "meterStop"), new QuantityType<>(request.getMeterStop(), Units.WATT_HOUR));

    return new StopTransactionConfirmation();
  }

  private static State parse(Double measurement, ChannelUID uid, SampledValue sample) {
    String unit = sample.getUnit();
    if (unit != null) {
      ComparableQuantity<?> quantity = Quantities.getQuantity("1 " + unit);
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
