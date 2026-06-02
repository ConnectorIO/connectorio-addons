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
  private final OcppRequestListener<Request> listener;

  public ChargerConnectorAdapter(OcppRequestListener<Request> listener) {
    this.listener = listener;
  }

  public void addConnector(int connector, ConnectorThingHandler handler) {
    handlers.put(connector, handler);
  }

  public void removeConnector(int connector) {
    handlers.remove(connector);
  }

  @Override
  public StatusNotificationConfirmation handleStatusNotification(StatusNotificationRequest request) {
    listener.onRequest(request);
    return handle(handler -> handler.handleStatusNotification(request), request.getConnectorId());
  }

  @Override
  public MeterValuesConfirmation handleMeterValues(MeterValuesRequest request) {
    listener.onRequest(request);
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
