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
import eu.chargetime.ocpp.JSONServer;
import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.ServerEvents;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.feature.profile.ServerCoreProfile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.SessionInformation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.BootConfigAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.HearbeatAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.AuthorizationIdTagAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.BootRegistrationAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.RequestListenerAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.StatusAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.TransactionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OcppServer implements OcppSender {

  private final Logger logger = LoggerFactory.getLogger(OcppServer.class);
  private final JSONServer server;
  private final String ip;
  private final int port;
  private final OcppChargerSessionRegistry chargerSessionRegistry;

  public OcppServer(String ip, int port, OcppChargerSessionRegistry chargerSessionRegistry,
      Deque<ServerCoreEventHandler> eventHandlers) {
    this.ip = ip;
    this.port = port;
    this.chargerSessionRegistry = chargerSessionRegistry;

    CoreEventHandlerWrapper handler = new CoreEventHandlerWrapper(eventHandlers);
    this.server = new JSONServer(new ServerCoreProfile(handler));
  }

  public void activate() {
    server.open(ip, port, new ServerEvents() {
      @Override
      public void newSession(UUID sessionIndex, SessionInformation information) {
        logger.info("New OCPP connection {} with identifier {} from address {}.", sessionIndex, information.getIdentifier(), information.getAddress());
      }

      @Override
      public void lostSession(UUID sessionIndex) {
        logger.info("Terminated connection {}.", sessionIndex);
        chargerSessionRegistry.removeSession(sessionIndex);
      }
    });
  }

  @Override
  public CompletionStage<Confirmation> send(ChargerReference chargerReference, Request request) {
    try {
      UUID sessionIndex = chargerSessionRegistry.getSession(chargerReference);
      if (sessionIndex == null) {
        logger.warn("Could not send request {} to charger {}. Session not found.", request, chargerReference);
        return CompletableFuture.failedFuture(new NotConnectedException());
      }
      return this.server.send(sessionIndex, request);
    } catch (OccurenceConstraintException | UnsupportedFeatureException | NotConnectedException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    if (!server.isClosed()) {
      server.close();
    }
  }

}
