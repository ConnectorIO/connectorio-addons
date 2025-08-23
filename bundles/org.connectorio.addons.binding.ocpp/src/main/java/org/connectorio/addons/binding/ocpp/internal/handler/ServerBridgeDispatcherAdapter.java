package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.core.HeartbeatConfirmation;
import eu.chargetime.ocpp.model.core.HeartbeatRequest;
import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.CoreEventHandlerAdapter;

public class ServerBridgeDispatcherAdapter extends CoreEventHandlerAdapter {

  private final Map<ChargerReference, ChargerThingHandler> handlers = new ConcurrentHashMap<>();
  private final OcppChargerSessionRegistry sessionRegistry;

  public ServerBridgeDispatcherAdapter(OcppChargerSessionRegistry sessionRegistry) {
    this.sessionRegistry = sessionRegistry;
  }

  public void addHandler(ChargerReference reference, ChargerThingHandler handler) {
    this.handlers.put(reference, handler);
  }

  public void removeHandler(ChargerReference reference) {
    this.handlers.remove(reference);
  }

  @Override
  public HeartbeatConfirmation handleHeartbeatRequest(UUID sessionIndex, HeartbeatRequest request) {
    return handle(sessionIndex, handler -> handler.handleHeartbeat(request));
  }

  @Override
  public MeterValuesConfirmation handleMeterValuesRequest(UUID sessionIndex, MeterValuesRequest request) {
    return handle(sessionIndex, handler -> handler.handleMeterValues(request));
  }

  @Override
  public StartTransactionConfirmation handleStartTransactionRequest(UUID sessionIndex, StartTransactionRequest request) {
    return handle(sessionIndex, handler -> handler.handleStartTransaction(request));
  }

  @Override
  public StatusNotificationConfirmation handleStatusNotificationRequest(UUID sessionIndex, StatusNotificationRequest request) {
    return handle(sessionIndex, handler -> handler.handleStatusNotification(request));
  }

  @Override
  public StopTransactionConfirmation handleStopTransactionRequest(UUID sessionIndex, StopTransactionRequest request) {
    return handle(sessionIndex, handler -> handler.handleStopTransaction(request));
  }

  private <C extends Confirmation> C handle(UUID sessionIndex, Function<ChargerThingHandler, C> consumer) {
    ChargerReference charger = sessionRegistry.getCharger(sessionIndex);
    if (charger != null) {
      ChargerThingHandler handler = handlers.get(charger);
      if (handler != null) {
        return consumer.apply(handler);
      }
    }
    return null;
  }

}
