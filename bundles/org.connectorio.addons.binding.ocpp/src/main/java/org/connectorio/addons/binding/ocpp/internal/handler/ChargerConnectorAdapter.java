package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.connectorio.addons.binding.ocpp.internal.server.listener.MeterValuesHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.StatusNotificationHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.TransactionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargerConnectorAdapter implements StatusNotificationHandler, MeterValuesHandler,
  TransactionHandler {

  private final Logger logger = LoggerFactory.getLogger(ChargerConnectorAdapter.class);
  private final Map<Integer, ConnectorThingHandler> handlers = new ConcurrentHashMap<>();
  private final Map<Integer, Integer> transactionMap = new ConcurrentHashMap<>();
  // Charger-wide transaction-id sequence shared with every connector so their ids never collide;
  // StopTransaction carries no connectorId, so the id is the only key back to the right connector.
  private final AtomicInteger transactionSequence = new AtomicInteger(1);
  private final OcppRequestListener<Request> listener;

  public ChargerConnectorAdapter(OcppRequestListener<Request> listener) {
    this.listener = listener;
  }

  public void addConnector(int connector, ConnectorThingHandler handler) {
    handler.setTransactionSequence(transactionSequence);
    handlers.put(connector, handler);
  }

  public void removeConnector(int connector) {
    handlers.remove(connector);
  }

  @Override
  public StatusNotificationConfirmation handleStatusNotification(StatusNotificationRequest request) {
    listener.onRequest(request);
    if (!handlers.containsKey(request.getConnectorId())) {
      // Charger-level (connectorId 0) or a connector with no Thing — ACK so the OCPP layer does
      // not answer the charge point with NotSupported.
      return new StatusNotificationConfirmation();
    }
    return handle(handler -> handler.handleStatusNotification(request), request.getConnectorId());
  }

  @Override
  public MeterValuesConfirmation handleMeterValues(MeterValuesRequest request) {
    listener.onRequest(request);
    if (!handlers.containsKey(request.getConnectorId())) {
      // Charger-level (connectorId 0, e.g. idle clock-aligned MeterValues) or a connector with no
      // Thing — ACK with an empty confirmation rather than letting the library reply NotSupported.
      return new MeterValuesConfirmation();
    }
    return handle(handler -> handler.handleMeterValues(request), request.getConnectorId());
  }

  @Override
  public StartTransactionConfirmation handleStartTransaction(StartTransactionRequest request) {
    listener.onRequest(request);

    StartTransactionConfirmation confirmation = handle(handler -> handler.handleStartTransaction(request), request.getConnectorId());
    if (confirmation != null) {
      transactionMap.put(request.getConnectorId(), confirmation.getTransactionId());
    }
    return confirmation;
  }

  @Override
  public StopTransactionConfirmation handleStopTransaction(StopTransactionRequest request) {
    listener.onRequest(request);
    Integer connectorId = null;
    for (Entry<Integer, Integer> entry : transactionMap.entrySet()) {
      if (entry.getValue().equals(request.getTransactionId())) {
        connectorId = entry.getKey();
        break;
      }
    }

    if (connectorId != null) {
      return handle(handler -> handler.handleStopTransaction(request), connectorId);
    }
    // Unknown transaction — no StartTransaction was tracked for this id. Common with free-charging
    // (FreeMode): the charger runs a local transaction and sends StopTransaction at session end without
    // a StartTransaction we ever saw. OCPP requires the CSMS to ACK StopTransaction regardless; reply
    // with an empty confirmation instead of returning null, which makes the library send a NotSupported
    // CallError — leaving the charger retrying or holding a dangling transaction (which can then block
    // the next StartTransaction).
    logger.info("StopTransaction for transaction {} has no matching StartTransaction; acknowledging it"
        + " without a per-connector state change (charger likely runs a local/free transaction).",
        request.getTransactionId());
    return new StopTransactionConfirmation();
  }

  private <C> C handle(Function<ConnectorThingHandler, C> handler, int connector) {
    if (handlers.containsKey(connector)) {
      ConnectorThingHandler connectorHandler = handlers.get(connector);
      return handler.apply(connectorHandler);
    }

    return null;
  }

}
