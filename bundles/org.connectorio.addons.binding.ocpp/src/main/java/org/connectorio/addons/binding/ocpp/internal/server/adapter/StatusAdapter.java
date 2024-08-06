package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import java.util.UUID;

public class StatusAdapter extends CoreEventHandlerAdapter {

  @Override
  public StatusNotificationConfirmation handleStatusNotificationRequest(UUID sessionIndex, StatusNotificationRequest request) {
    // return super.handleStatusNotificationRequest(sessionIndex, request);

    System.out.println("Status " + request);

    return new StatusNotificationConfirmation();
  }
}
