package org.connectorio.addons.binding.ocpp.internal.server.listener;

import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;

public interface MeterValuesHandler {

  MeterValuesConfirmation handleMeterValues(MeterValuesRequest request);

}
