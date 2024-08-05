package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.HeartbeatConfirmation;
import eu.chargetime.ocpp.model.core.HeartbeatRequest;
import java.time.ZonedDateTime;
import java.util.UUID;

public class HearbeatAdapter extends CoreEventHandlerAdapter {

  @Override
  public HeartbeatConfirmation handleHeartbeatRequest(UUID sessionIndex, HeartbeatRequest request) {
    //System.out.println(request);
    return new HeartbeatConfirmation(ZonedDateTime.now());
  }
}
