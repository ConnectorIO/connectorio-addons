package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import java.util.UUID;

public class TransactionAdapter extends CoreEventHandlerAdapter {

  @Override
  public StartTransactionConfirmation handleStartTransactionRequest(UUID sessionIndex, StartTransactionRequest request) {
    // return super.handleStartTransactionRequest(sessionIndex, request);
    System.out.println("Start transaction " + request);
    return new StartTransactionConfirmation();
  }

  @Override
  public StopTransactionConfirmation handleStopTransactionRequest(UUID sessionIndex, StopTransactionRequest request) {
    //return super.handleStopTransactionRequest(sessionIndex, request);
    System.out.println("Stop transaction " + request);
    return new StopTransactionConfirmation();
  }
}
