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

public class ChargerConnectorAdapter implements StatusNotificationHandler, MeterValuesHandler,
  TransactionHandler {

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
    // unknown transaction
    return null;
  }

  private <C> C handle(Function<ConnectorThingHandler, C> handler, int connector) {
    if (handlers.containsKey(connector)) {
      ConnectorThingHandler connectorHandler = handlers.get(connector);
      return handler.apply(connectorHandler);
    }

    return null;
  }

}
