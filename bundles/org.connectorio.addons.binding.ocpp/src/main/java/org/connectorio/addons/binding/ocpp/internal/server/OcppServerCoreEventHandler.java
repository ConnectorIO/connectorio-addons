/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.ocpp.internal.server;

import eu.chargetime.ocpp.feature.profile.ServerCoreEventHandler;
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
import eu.chargetime.ocpp.model.core.RegistrationStatus;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.eclipse.jetty.client.api.Request.RequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class OcppServerCoreEventHandler implements ServerCoreEventHandler {

  private final Logger logger = LoggerFactory.getLogger(OcppServerCoreEventHandler.class);
  private OcppSender sender;
  private OcppRequestListener<Request> listener;
  private final Set<String> identifiers;
  private final Set<String> tags;

  public OcppServerCoreEventHandler(OcppSender sender, OcppRequestListener<Request> listener, Set<String> identifiers, Set<String> tags) {
    this.sender = sender;
    this.listener = listener;
    this.identifiers = identifiers;
    this.tags = tags;
  }

  @Override
  public AuthorizeConfirmation handleAuthorizeRequest(UUID sessionIndex, AuthorizeRequest request) {
    request.getIdTag();
    return null;
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex, BootNotificationRequest request) {
    if (identifiers.isEmpty()) {
      listener.onRequest(request);
      return new BootNotificationConfirmation(ZonedDateTime.now(), 1000, RegistrationStatus.Accepted);
    }
    if (identifiers.contains(request.getMeterSerialNumber())) {
      listener.onRequest(request);
      return new BootNotificationConfirmation(ZonedDateTime.now(), 1000, RegistrationStatus.Accepted);
    }

    // skip notifying listeners
    return new BootNotificationConfirmation(ZonedDateTime.now(), 1000, RegistrationStatus.Rejected);
  }

  @Override
  public DataTransferConfirmation handleDataTransferRequest(UUID sessionIndex, DataTransferRequest request) {
    return null;
  }

  @Override
  public HeartbeatConfirmation handleHeartbeatRequest(UUID sessionIndex, HeartbeatRequest request) {
    return new HeartbeatConfirmation(ZonedDateTime.now());
  }

  @Override
  public MeterValuesConfirmation handleMeterValuesRequest(UUID sessionIndex, MeterValuesRequest request) {
    logger.info("Received meter values for session {}: {}", sessionIndex, request);
    return new MeterValuesConfirmation();
  }

  @Override
  public StartTransactionConfirmation handleStartTransactionRequest(UUID sessionIndex, StartTransactionRequest request) {
    return null;
  }

  @Override
  public StatusNotificationConfirmation handleStatusNotificationRequest(UUID sessionIndex, StatusNotificationRequest request) {
    return new StatusNotificationConfirmation();
  }

  @Override
  public StopTransactionConfirmation handleStopTransactionRequest(UUID sessionIndex, StopTransactionRequest request) {
    return null;
  }
}
