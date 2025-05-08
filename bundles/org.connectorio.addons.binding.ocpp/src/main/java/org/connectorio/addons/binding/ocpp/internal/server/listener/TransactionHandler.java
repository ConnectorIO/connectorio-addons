package org.connectorio.addons.binding.ocpp.internal.server.listener;

import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

public interface TransactionHandler {

  StartTransactionConfirmation handleStartTransaction(StartTransactionRequest request);
  StopTransactionConfirmation handleStopTransaction(StopTransactionRequest request);

}
