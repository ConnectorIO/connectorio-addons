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

import eu.chargetime.ocpp.JSONServer;
import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.ServerEvents;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.feature.profile.ServerCoreProfile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.SessionInformation;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OcppServer implements OcppSender {

  private final Logger logger = LoggerFactory.getLogger(OcppServer.class);
  private final JSONServer server;
  private final String ip;
  private final int port;

  public OcppServer(String ip, int port, OcppRequestListener<Request> listener, Set<String> identifiers, Set<String> tags) {
    this.ip = ip;
    this.port = port;
    OcppServerCoreEventHandler eventHandler = new OcppServerCoreEventHandler(this, listener, identifiers, tags);
    this.server = new JSONServer(new ServerCoreProfile(eventHandler));
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
      }
    });
  }

  @Override
  public CompletionStage<Confirmation> send(UUID sessionIndex, Request request) {
    try {
      return this.server.send(sessionIndex, request);
    } catch (OccurenceConstraintException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedFeatureException e) {
      throw new RuntimeException(e);
    } catch (NotConnectedException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    if (!server.isClosed()) {
      server.close();
    }
  }

  public static void main(String[] args) {
    OcppServer ocppServer = new OcppServer("127.0.0.1", 8888, null, Collections.emptySet(), Collections.emptySet());
    ocppServer.activate();
  }

}
