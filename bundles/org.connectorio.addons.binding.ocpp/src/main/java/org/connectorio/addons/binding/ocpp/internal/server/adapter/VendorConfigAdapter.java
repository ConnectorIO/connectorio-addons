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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VendorConfigAdapter extends CoreEventHandlerAdapter {

  private final Logger logger = LoggerFactory.getLogger(VendorConfigAdapter.class);
  private final OcppChargerSessionRegistry sessionRegistry;
  private final OcppSender sender;
  private final Map<String, String> vendorKeys;

  public VendorConfigAdapter(OcppChargerSessionRegistry sessionRegistry, OcppSender sender,
      Map<String, String> vendorKeys) {
    this.sessionRegistry = sessionRegistry;
    this.sender = sender;
    this.vendorKeys = vendorKeys;
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex, BootNotificationRequest request) {
    if (vendorKeys == null || vendorKeys.isEmpty()) {
      return null;
    }
    ChargerReference reference = sessionRegistry.getCharger(sessionIndex);
    if (reference == null) {
      return null;
    }
    for (Map.Entry<String, String> entry : vendorKeys.entrySet()) {
      apply(reference, entry.getKey(), entry.getValue());
    }
    return null;
  }

  private void apply(ChargerReference reference, String key, String value) {
    sender.send(reference, new ChangeConfigurationRequest(key, value)).whenComplete((confirmation, ex) -> {
      if (ex != null) {
        logger.warn("ChangeConfiguration[{}={}] for {} failed: {}", key, value, reference, ex.getMessage());
      } else {
        logger.debug("ChangeConfiguration[{}={}] for {}: {}", key, value, reference, confirmation);
      }
    });
  }

  /**
   * Parse a list of {@code key=value} strings into a map. Empty entries and entries without
   * {@code =} are ignored. Whitespace around key and value is trimmed.
   */
  public static Map<String, String> parse(List<String> entries) {
    Map<String, String> map = new java.util.LinkedHashMap<>();
    if (entries == null) {
      return map;
    }
    for (String entry : entries) {
      if (entry == null) {
        continue;
      }
      int eq = entry.indexOf('=');
      if (eq <= 0 || eq == entry.length() - 1) {
        continue;
      }
      String key = entry.substring(0, eq).trim();
      String value = entry.substring(eq + 1).trim();
      if (!key.isEmpty()) {
        map.put(key, value);
      }
    }
    return map;
  }
}
