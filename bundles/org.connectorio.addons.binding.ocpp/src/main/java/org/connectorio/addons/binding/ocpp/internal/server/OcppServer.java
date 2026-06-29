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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
   * WebSocket closes mid-flight — see ChargeTimeEU/Java-OCA-OCPP#121. Bounding
   * the wait with {@code Future.get(timeout)} surfaces the failure as a
   * {@link TimeoutException} instead of leaving the caller waiting forever.
   */
  private static final long CALL_TIMEOUT_SECONDS = 15;

  private final Logger logger = LoggerFactory.getLogger(OcppServer.class);
  private final JSONServer server;
  private final String ip;
  private final int port;
  private final OcppChargerSessionRegistry chargerSessionRegistry;
  private final OcularSolarEcoMode ocularSolarEcoMode;

  /**
   * Chargers we have already sent TriggerMessage(BootNotification) to once. Chargers that honor it
   * re-cycle their connection in a loop if it is re-sent on every reconnect, so it is sent at most
   * once per charger, immediately on first connect. StatusNotification is handled separately — see
   * {@link #scheduleStateRefresh}: it is (re)requested only once a session has been stable for
   * {@link #STATE_REFRESH_SETTLE_SECONDS}, so a flapping charger never gets flooded.
   */
  private final Set<String> bootTriggered = ConcurrentHashMap.newKeySet();

  /**
   * How long a session must stay up before we (re)request StatusNotification. A charger that flaps
   * (reconnecting every few tens of seconds) never reaches this, so we stop flooding it with
   * TriggerMessage CALLs it cannot answer (which also appears to shorten its reconnect cycle) and
   * stop the resulting timeout-WARN spam. A stable or charging session DOES reach it, keeping the
   * connector status in sync — a stale Available-while-charging makes RemoteStart be Rejected and
   * power read NaN. The charger's own spontaneous StatusNotification still covers real state
   * changes; this scheduled trigger is only the fallback re-sync.
   */
  private static final long STATE_REFRESH_SETTLE_SECONDS = 90;
  private final ScheduledExecutorService refreshScheduler =
      Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "ocpp-state-refresh");
        thread.setDaemon(true);
        return thread;
      });
  private final Map<UUID, ScheduledFuture<?>> pendingStatusRefresh = new ConcurrentHashMap<>();

  /**
   * One {@link SessionSender} per live session, each owning a single worker thread that serialises
   * that charger's outbound CALLs. OCPP 1.6 permits only one in-flight request per direction, and a
   * strict charger drops the WebSocket if it receives a concurrent burst (observed on the Wallbox
   * Copper SB during the boot-time configuration burst).
   */
  private final Map<UUID, SessionSender> sessionSenders = new ConcurrentHashMap<>();

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
        
        // Stand up the per-session sender before the registry entry so any send triggered below (or
        // by a racing caller) finds its serialising worker rather than failing as "session not found".
        SessionSender previous = sessionSenders.put(sessionIndex, new SessionSender(sessionIndex));
        if (previous != null) {
          previous.close(); // never orphan a worker thread if the same id ever reconnects without a lost event
        }
        chargerSessionRegistry.registerSession(sessionIndex,
            new ChargerReference(identifier));

        ChargerReference reference = new ChargerReference(identifier);
        ocularSolarEcoMode.applyOcularEcoMode(reference);
        scheduleStateRefresh(sessionIndex, reference);
      }

      @Override
      public void lostSession(UUID sessionIndex) {
        logger.info("Terminated connection {}.", sessionIndex);
        chargerSessionRegistry.removeSession(sessionIndex);
        // Cancel a not-yet-fired StatusNotification refresh — this session flapped before it settled,
        // so re-probing it would just flood a flapping charger.
        ScheduledFuture<?> pending = pendingStatusRefresh.remove(sessionIndex);
        if (pending != null) {
          pending.cancel(false);
        }
        SessionSender sender = sessionSenders.remove(sessionIndex);
        if (sender != null) {
          sender.close();
        }
      }
    });
  }

  @Override
  public CompletionStage<Confirmation> send(ChargerReference chargerReference, Request request) {
    UUID sessionIndex = chargerSessionRegistry.getSession(chargerReference);
    if (sessionIndex == null) {
      logger.warn("Could not send request {} to charger {}. Session not found.", request, chargerReference);
      return CompletableFuture.failedFuture(new NotConnectedException());
    }
    SessionSender sender = sessionSenders.get(sessionIndex);
    if (sender == null) {
      logger.warn("Could not send request {} to charger {}. Session is closing.", request, chargerReference);
      return CompletableFuture.failedFuture(new NotConnectedException());
    }
    return sender.submit(chargerReference, request);
  }

  public void close() {
    refreshScheduler.shutdownNow();
    sessionSenders.values().forEach(SessionSender::close);
    sessionSenders.clear();
    if (!server.isClosed()) {
      server.close();
    }
  }

  private void scheduleStateRefresh(UUID sessionIndex, ChargerReference reference) {
    // BootNotification at most once per charger, immediately: chargers that honor
    // TriggerMessage(BootNotification) re-cycle their connection in a loop if it is re-sent on every
    // reconnect. Guarded once, it does not flood.
    if (bootTriggered.add(reference.getSerial())) {
      sendTrigger(reference, TriggerMessageRequestType.BootNotification);
    }
    // StatusNotification only once the session has stayed up for STATE_REFRESH_SETTLE_SECONDS. A
    // flapping charger never reaches it — so we no longer flood it with TriggerMessage CALLs it can't
    // answer, killing the timeout-WARN spam and easing the flap. A stable or charging session does
    // reach it, re-syncing a stale status. Cancelled in lostSession if the session drops first.
    ScheduledFuture<?> future = refreshScheduler.schedule(() -> {
      pendingStatusRefresh.remove(sessionIndex);
      // Only if this exact session is still the charger's current one (it didn't flap away meanwhile).
      if (sessionIndex.equals(chargerSessionRegistry.getSession(reference))) {
        sendTrigger(reference, TriggerMessageRequestType.StatusNotification);
      }
    }, STATE_REFRESH_SETTLE_SECONDS, TimeUnit.SECONDS);
    pendingStatusRefresh.put(sessionIndex, future);
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

  /**
   * Serialises outbound CALLs to one charger over a dedicated worker thread.
   *
   * <p>OCPP 1.6 permits only one in-flight request per direction, and a strict charger (the Wallbox
   * Copper SB observed here) drops its WebSocket if it receives a concurrent burst — e.g. the
   * boot-time configuration burst. Each session owns a single daemon thread that takes requests off
   * its queue and sends them one at a time, blocking on the charger's answer (bounded by
   * {@link #CALL_TIMEOUT_SECONDS}) before starting the next. The work stays on this per-session
   * thread rather than the shared {@link CompletableFuture} pool, the per-CALL result futures are
   * dropped from {@link #inFlight} as soon as they settle so nothing accumulates, and when the
   * session ends every queued or in-flight CALL is failed at once instead of hanging until it times
   * out on a socket that is already gone.
   *
   * <p>Continuations on the stage returned by {@link #submit} run on this worker thread, so callers
   * must keep them non-blocking and must not synchronously await another CALL to the same session.
   */
  private final class SessionSender {

    private final UUID sessionIndex;
    private final ExecutorService worker;
    private final Set<CompletableFuture<Confirmation>> inFlight = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    SessionSender(UUID sessionIndex) {
      this.sessionIndex = sessionIndex;
      this.worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ocpp-send-" + sessionIndex);
        thread.setDaemon(true);
        return thread;
      });
    }

    CompletableFuture<Confirmation> submit(ChargerReference reference, Request request) {
      CompletableFuture<Confirmation> result = new CompletableFuture<>();
      if (closed) {
        result.completeExceptionally(new NotConnectedException());
        return result;
      }
      // Track only what is still pending — release each future the moment it settles so completed
      // CALLs are not retained.
      inFlight.add(result);
      result.whenComplete((confirmation, throwable) -> inFlight.remove(result));
      try {
        worker.execute(() -> deliver(reference, request, result));
      } catch (RejectedExecutionException e) {
        result.completeExceptionally(new NotConnectedException());
      }
      return result;
    }

    private void deliver(ChargerReference reference, Request request,
        CompletableFuture<Confirmation> result) {
      if (closed) {
        result.completeExceptionally(new NotConnectedException());
        return;
      }
      if (result.isDone()) {
        return; // failed by close() while still queued
      }
      try {
        // Single worker thread => exactly one CALL in flight per session. Block here on the answer so
        // the next queued CALL only starts once this one has been acknowledged.
        Confirmation confirmation = server.send(sessionIndex, request)
            .toCompletableFuture()
            .get(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        result.complete(confirmation);
      } catch (TimeoutException e) {
        // chargetime/ocpp does not cancel the timed-out CALL (ChargeTimeEU/Java-OCA-OCPP#121), so it is
        // still registered on the wire. Dispatching the next CALL would put two in flight at once — the
        // very burst a strict charger drops on. Close the session instead; the charger reconnects and we
        // resume on a fresh, known-clear socket.
        logger.warn("Request {} to charger {} timed out after {} s; closing the session to avoid a"
                + " concurrent in-flight CALL (ChargeTimeEU/Java-OCA-OCPP#121).",
            request.getClass().getSimpleName(), reference, CALL_TIMEOUT_SECONDS);
        result.completeExceptionally(e);
        closed = true;
        server.closeSession(sessionIndex);
      } catch (ExecutionException e) {
        result.completeExceptionally(e.getCause() != null ? e.getCause() : e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        result.completeExceptionally(e);
      } catch (OccurenceConstraintException | UnsupportedFeatureException | NotConnectedException e) {
        result.completeExceptionally(e);
      } finally {
        // Never leave a caller hanging: an unchecked throw from server.send() (e.g. the WebSocket
        // transmitter on a half-closed socket) still settles the future here.
        if (!result.isDone()) {
          result.completeExceptionally(new NotConnectedException());
        }
      }
    }

    void close() {
      closed = true;
      worker.shutdownNow(); // stop the worker; interrupts a blocking get(), drops queued CALLs
      NotConnectedException terminated = new NotConnectedException();
      for (CompletableFuture<Confirmation> pending : inFlight) {
        pending.completeExceptionally(terminated);
      }
      inFlight.clear();
    }
  }

}
