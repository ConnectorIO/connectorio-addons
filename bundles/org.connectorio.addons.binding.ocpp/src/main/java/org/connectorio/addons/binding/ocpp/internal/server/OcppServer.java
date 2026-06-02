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

import eu.chargetime.ocpp.feature.profile.ServerSmartChargingProfile;
import eu.chargetime.ocpp.feature.profile.ServerRemoteTriggerProfile;
import eu.chargetime.ocpp.feature.profile.ServerCoreEventHandler;
import eu.chargetime.ocpp.JSONConfiguration;
import eu.chargetime.ocpp.JSONServer;
import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.ServerEvents;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.feature.profile.ServerCoreProfile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.SessionInformation;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.custom.OcularSolarEcoMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OcppServer implements OcppSender {

  /**
   * Maximum time to wait for a charger to answer a CSMS-initiated CALL.
   * chargetime/ocpp's {@code Server.send(...)} returns a future from
   * {@code PromiseRepository} that is silently orphaned when the underlying
   * WebSocket closes mid-flight — see ChargeTimeEU/Java-OCA-OCPP#121. Wrapping
   * with {@code orTimeout} surfaces the failure as a
   * {@link TimeoutException} instead of leaving the caller waiting forever.
   */
  private static final long CALL_TIMEOUT_SECONDS = 15;

  private final Logger logger = LoggerFactory.getLogger(OcppServer.class);
  private final JSONServer server;
  private final String ip;
  private final int port;
  private final OcppChargerSessionRegistry chargerSessionRegistry;
  private final OcularSolarEcoMode ocularSolarEcoMode;

  public OcppServer(String ip, int port, OcppChargerSessionRegistry chargerSessionRegistry,
      Deque<ServerCoreEventHandler> eventHandlers, OcularSolarEcoMode ocularSolarEcoMode,
      int pingIntervalSec) {
    this.ip = ip;
    this.port = port;
    this.chargerSessionRegistry = chargerSessionRegistry;
    this.ocularSolarEcoMode = ocularSolarEcoMode;
    ocularSolarEcoMode.setOcppSender(this);

    CoreEventHandlerWrapper handler = new CoreEventHandlerWrapper(eventHandlers);
    if (pingIntervalSec > 0) {
      JSONConfiguration jsonConfig = JSONConfiguration.get().setParameter(JSONConfiguration.PING_INTERVAL_PARAMETER, pingIntervalSec);
      this.server = new JSONServer(new ServerCoreProfile(handler), jsonConfig);
      logger.debug("OCPP server WebSocket PING interval set to {}s", pingIntervalSec);
    } else {
      this.server = new JSONServer(new ServerCoreProfile(handler));
    }

    // Register the SERVER smart-charging profile so the CSMS can SEND SetChargingProfile /
    // ClearChargingProfile / GetCompositeSchedule. (Previously the *client* profile was registered,
    // which is the charge-point side and does not let the server issue smart-charging CALLs.)
    this.server.addFeatureProfile(new ServerSmartChargingProfile());
    this.server.addFeatureProfile(new ServerRemoteTriggerProfile());
  }

  public void activate() {
    server.open(ip, port, new ServerEvents() {
      @Override
      public void newSession(UUID sessionIndex, SessionInformation information) {
        logger.info("New OCPP connection {} with identifier {} from address {}.", sessionIndex, information.getIdentifier(), information.getAddress());

        // Register session immediately on connect, don't wait for BootNotification
        // as some chargers sends the BootNotification only if the charger is rebooted.
        String identifier = information.getIdentifier();
        if (identifier != null && identifier.startsWith("/")) {
            identifier = identifier.substring(1);
        }
        
        chargerSessionRegistry.registerSession(sessionIndex,
            new ChargerReference(identifier));

        ChargerReference reference = new ChargerReference(identifier);
        ocularSolarEcoMode.applyOcularEcoMode(reference);
        triggerStateRefresh(reference);
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
      return this.server.send(sessionIndex, request)
          .toCompletableFuture()
          .orTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
          .whenComplete((confirmation, throwable) -> {
            if (throwable instanceof TimeoutException) {
              logger.warn("Request {} to charger {} timed out after {} s — chargetime/ocpp future may have been"
                      + " orphaned by socket close (ChargeTimeEU/Java-OCA-OCPP#121).",
                  request.getClass().getSimpleName(), chargerReference, CALL_TIMEOUT_SECONDS);
            }
          });
    } catch (OccurenceConstraintException | UnsupportedFeatureException | NotConnectedException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    if (!server.isClosed()) {
      server.close();
    }
  }

  private void triggerStateRefresh(ChargerReference reference) {
    sendTrigger(reference, TriggerMessageRequestType.StatusNotification);
    sendTrigger(reference, TriggerMessageRequestType.BootNotification);
  }

  private void sendTrigger(ChargerReference reference, TriggerMessageRequestType type) {
    TriggerMessageRequest request = new TriggerMessageRequest(type);
    send(reference, request).whenComplete((confirmation, throwable) -> {
      if (throwable != null) {
        logger.debug("TriggerMessage({}) for {} failed: {}", type, reference, throwable.getMessage());
      } else {
        logger.debug("TriggerMessage({}) for {}: {}", type, reference, confirmation);
      }
    });
  }

}
