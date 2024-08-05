package org.connectorio.addons.binding.ocpp.internal.server.listener;

import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;

public interface StatusNotificationHandler {

  StatusNotificationConfirmation handleStatusNotification(StatusNotificationRequest request);

}
