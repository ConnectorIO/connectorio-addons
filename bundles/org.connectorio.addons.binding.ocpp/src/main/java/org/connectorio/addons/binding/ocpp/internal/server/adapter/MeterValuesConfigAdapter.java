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
package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.ConfigurationStatus;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeterValuesConfigAdapter extends CoreEventHandlerAdapter {

  private final Logger logger = LoggerFactory.getLogger(MeterValuesConfigAdapter.class);
  private final OcppChargerSessionRegistry sessionRegistry;
  private final OcppSender sender;
  private final int sampleInterval;
  private final String sampledData;
  private final int clockAlignedInterval;

  /**
   * Package-scoped for testability. Keyed by charger serial: the measurand list this charger has
   * last accepted. OCPP 1.6 has no way to enumerate a charger's supported measurands up front — the
   * only signal that one is unsupported is the ChangeConfiguration Reject itself — so the accepted
   * set is discovered once by elimination and reused on every later (re)connect instead of
   * re-discovering it from scratch each time.
   */
  final Map<String, String> acceptedMeasurands = new ConcurrentHashMap<>();

  public MeterValuesConfigAdapter(OcppChargerSessionRegistry sessionRegistry, OcppSender sender,
      int sampleInterval, String sampledData, int clockAlignedInterval) {
    this.sessionRegistry = sessionRegistry;
    this.sender = sender;
    this.sampleInterval = sampleInterval;
    this.sampledData = sampledData;
    this.clockAlignedInterval = clockAlignedInterval;
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex, BootNotificationRequest request) {
    ChargerReference reference = sessionRegistry.getCharger(sessionIndex);
    if (reference == null) {
      return null;
    }
    String measurands = acceptedMeasurands.getOrDefault(reference.getSerial(), sampledData);
    apply(reference, "MeterValueSampleInterval", Integer.toString(sampleInterval));
    apply(reference, "MeterValuesSampledData", measurands);
    apply(reference, "MeterValuesAlignedData", measurands);
    apply(reference, "ClockAlignedDataInterval", Integer.toString(clockAlignedInterval));
    return null;
  }

  private void apply(ChargerReference reference, String key, String value) {
    boolean isMeterMeasurandKey = "MeterValuesSampledData".equals(key) || "MeterValuesAlignedData".equals(key);
    sender.send(reference, new ChangeConfigurationRequest(key, value)).whenComplete((confirmation, ex) -> {
      if (ex != null) {
        logger.warn("ChangeConfiguration[{}] for {} failed: {}", key, reference, ex.getMessage());
        return;
      }
      if (isMeterMeasurandKey && confirmation instanceof ChangeConfigurationConfirmation
          && ((ChangeConfigurationConfirmation) confirmation).getStatus() == ConfigurationStatus.Rejected) {
        String stripped = stripLast(value);
        if (!stripped.equals(value) && !stripped.isEmpty()) {
          logger.info("Charger {} rejected ChangeConfiguration[{}={}] — retrying with one fewer measurand: {}",
              reference, key, value, stripped);
          apply(reference, key, stripped);
          return;
        }
        logger.warn("Charger {} rejected ChangeConfiguration[{}] down to a single measurand; giving up",
            reference, key);
        return;
      }
      if (isMeterMeasurandKey) {
        acceptedMeasurands.put(reference.getSerial(), value);
      }
      logger.debug("ChangeConfiguration[{}={}] for {}: {}", key, value, reference, confirmation);
    });
  }

  /**
   * Return {@code value} with its last comma-separated measurand removed (remaining tokens
   * trimmed), or {@code value} unchanged if it is null/empty. Trial-based elimination: OCPP 1.6
   * gives no signal on which measurand a Reject was actually about, so candidates are tried by
   * dropping one at a time rather than by any hardcoded fragile-measurand list — no measurand name
   * is baked into the binding.
   */
  static String stripLast(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    String[] tokens = value.split(",");
    if (tokens.length <= 1) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tokens.length - 1; i++) {
      if (sb.length() > 0) {
        sb.append(',');
      }
      sb.append(tokens[i].trim());
    }
    return sb.toString();
  }
}
