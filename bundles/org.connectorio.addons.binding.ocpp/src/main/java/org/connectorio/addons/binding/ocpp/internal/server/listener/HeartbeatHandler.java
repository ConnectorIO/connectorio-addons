package org.connectorio.addons.binding.ocpp.internal.server.listener;

import eu.chargetime.ocpp.model.core.HeartbeatConfirmation;
import eu.chargetime.ocpp.model.core.HeartbeatRequest;

public interface HeartbeatHandler {

  HeartbeatConfirmation handleHeartbeat(HeartbeatRequest request);

}
