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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeterValuesConfigAdapter extends CoreEventHandlerAdapter {

  /**
   * Measurands often advertised but not implemented by individual charger firmwares. When a
   * charger rejects a ChangeConfiguration containing a sampled-data list, we strip the first
   * matching measurand from the list and retry — repeatedly if needed. Phoenix Contact CHARX
   * SEC-3xxx (FW 1.9.0) rejects {@code Temperature} (no internal sensor) and sometimes
   * {@code Power.Offered}. Order = strip priority (most fragile first).
   */
  private static final List<String> FRAGILE_MEASURANDS = Arrays.asList("Temperature", "Power.Offered");

  private final Logger logger = LoggerFactory.getLogger(MeterValuesConfigAdapter.class);
  private final OcppChargerSessionRegistry sessionRegistry;
  private final OcppSender sender;
  private final int sampleInterval;
  private final String sampledData;
  private final int clockAlignedInterval;

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
    apply(reference, "MeterValueSampleInterval", Integer.toString(sampleInterval));
    apply(reference, "MeterValuesSampledData", sampledData);
    apply(reference, "MeterValuesAlignedData", sampledData);
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
        String stripped = stripFirstFragile(value);
        if (!stripped.equals(value) && !stripped.isEmpty()) {
          logger.info("Charger {} rejected ChangeConfiguration[{}={}] — retrying as {}", reference, key, value, stripped);
          apply(reference, key, stripped);
          return;
        }
      }
      logger.debug("ChangeConfiguration[{}={}] for {}: {}", key, value, reference, confirmation);
    });
  }

  /**
   * Return {@code value} with the first matching fragile measurand removed (commas reconciled), or
   * {@code value} unchanged if none of the fragile measurands appear. Order follows
   * {@link #FRAGILE_MEASURANDS}.
   */
  static String stripFirstFragile(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    String[] tokens = value.split(",");
    for (String fragile : FRAGILE_MEASURANDS) {
      for (int i = 0; i < tokens.length; i++) {
        if (fragile.equals(tokens[i].trim())) {
          StringBuilder sb = new StringBuilder();
          for (int j = 0; j < tokens.length; j++) {
            if (j == i) {
              continue;
            }
            if (sb.length() > 0) {
              sb.append(',');
            }
            sb.append(tokens[j].trim());
          }
          return sb.toString();
        }
      }
    }
    return value;
  }
}
