/*
 * Copyright (C) 2022-2026 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.GetConfigurationRequest;
import eu.chargetime.ocpp.model.core.KeyValueType;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Side-effect-only event handler that, on every {@code BootNotification},
 * fires a one-shot {@code GetConfiguration} (no key list = "all keys") to
 * the charger and logs each returned key/value/readonly at {@code INFO}.
 *
 * <p>Returns {@code null} from all event handler methods, so it never
 * influences the chain's {@code Confirmation} (the active responder remains
 * {@link BootRegistrationAdapter}).
 *
 * <p>This is a diagnostic helper. It makes vendor-specific configuration
 * keys (e.g. Wallbox's {@code chargingALimitConn1}, Alfen's
 * {@code OperativeMaxCurrent}, KEBA's {@code MaxCurrentInternalLimit}) plus
 * the standard OCPP keys (e.g. {@code MeterValueSampleInterval},
 * {@code MeterValuesSampledData}, {@code WebSocketPingInterval}) visible in
 * the openHAB log without having to attach a separate OCPP debugger.
 */
public class OcppConfigurationDumpAdapter extends CoreEventHandlerAdapter {

  /**
   * Delay before firing {@code GetConfiguration}. Lets the
   * {@code BootNotificationConfirmation} reach the charger first so the
   * charger has fully transitioned to "registered" state before we ask
   * follow-up questions.
   */
  private static final long DUMP_DELAY_SECONDS = 2;

  private final Logger logger = LoggerFactory.getLogger(OcppConfigurationDumpAdapter.class);
  private final OcppSender sender;
  private final OcppChargerSessionRegistry registry;

  public OcppConfigurationDumpAdapter(OcppSender sender, OcppChargerSessionRegistry registry) {
    this.sender = sender;
    this.registry = registry;
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex,
      BootNotificationRequest request) {
    ChargerReference charger = registry.getCharger(sessionIndex);
    if (charger == null) {
      // Boot before registration; nothing to dump.
      return null;
    }
    CompletableFuture.runAsync(() -> dump(charger),
        CompletableFuture.delayedExecutor(DUMP_DELAY_SECONDS, TimeUnit.SECONDS));
    return null;
  }

  private void dump(ChargerReference charger) {
    try {
      sender.send(charger, new GetConfigurationRequest())
          .whenComplete((confirmation, throwable) -> {
            if (throwable != null) {
              logger.debug("GetConfiguration dump for {} failed: {}", charger, throwable.toString());
              return;
            }
            if (!(confirmation instanceof GetConfigurationConfirmation)) {
              return;
            }
            GetConfigurationConfirmation conf = (GetConfigurationConfirmation) confirmation;
            KeyValueType[] keys = conf.getConfigurationKey();
            if (keys != null) {
              for (KeyValueType kv : keys) {
                logger.info("GetConfiguration[{}]: {} = {} (readonly={})",
                    charger, kv.getKey(), kv.getValue(), kv.getReadonly());
              }
            }
            String[] unknown = conf.getUnknownKey();
            if (unknown != null && unknown.length > 0) {
              logger.info("GetConfiguration[{}] unknown keys: {}", charger, String.join(",", unknown));
            }
          });
    } catch (Exception e) {
      logger.debug("Could not send GetConfiguration to {}: {}", charger, e.toString());
    }
  }
}
