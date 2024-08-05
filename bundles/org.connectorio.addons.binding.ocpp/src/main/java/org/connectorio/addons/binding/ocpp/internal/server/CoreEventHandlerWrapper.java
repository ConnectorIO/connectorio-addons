package org.connectorio.addons.binding.ocpp.internal.server;

import eu.chargetime.ocpp.feature.profile.ServerCoreEventHandler;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.AuthorizeConfirmation;
import eu.chargetime.ocpp.model.core.AuthorizeRequest;
import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.DataTransferConfirmation;
import eu.chargetime.ocpp.model.core.DataTransferRequest;
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
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class CoreEventHandlerWrapper implements ServerCoreEventHandler {

  private final Deque<ServerCoreEventHandler> handlers;

  public CoreEventHandlerWrapper(Deque<ServerCoreEventHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public AuthorizeConfirmation handleAuthorizeRequest(UUID sessionIndex, AuthorizeRequest request) {
    return process(handler -> handler.handleAuthorizeRequest(sessionIndex, request));
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex, BootNotificationRequest request) {
    return process(handler -> handler.handleBootNotificationRequest(sessionIndex, request));
  }

  @Override
  public DataTransferConfirmation handleDataTransferRequest(UUID sessionIndex, DataTransferRequest request) {
    return process(handler -> handler.handleDataTransferRequest(sessionIndex, request));
  }

  @Override
  public HeartbeatConfirmation handleHeartbeatRequest(UUID sessionIndex, HeartbeatRequest request) {
    return process(handler -> handler.handleHeartbeatRequest(sessionIndex, request));
  }

  @Override
  public MeterValuesConfirmation handleMeterValuesRequest(UUID sessionIndex, MeterValuesRequest request) {
    return process(handler -> handler.handleMeterValuesRequest(sessionIndex, request));
  }

  @Override
  public StartTransactionConfirmation handleStartTransactionRequest(UUID sessionIndex, StartTransactionRequest request) {
    return process(handler -> handler.handleStartTransactionRequest(sessionIndex, request));
  }

  @Override
  public StatusNotificationConfirmation handleStatusNotificationRequest(UUID sessionIndex, StatusNotificationRequest request) {
    return process(handler -> handler.handleStatusNotificationRequest(sessionIndex, request));
  }

  @Override
  public StopTransactionConfirmation handleStopTransactionRequest(UUID sessionIndex, StopTransactionRequest request) {
    return process(handler -> handler.handleStopTransactionRequest(sessionIndex, request));
  }

  private <T extends Request, C extends Confirmation> C process(Function<ServerCoreEventHandler, C> consumer) {
    C confirmation = null;
    for (ServerCoreEventHandler handler : handlers) {
      C handlerConfirmation = consumer.apply(handler);
      if (confirmation == null && handlerConfirmation != null && handlerConfirmation.validate()) {
        confirmation = handlerConfirmation;
      }
    }
    return confirmation;
  }

}
