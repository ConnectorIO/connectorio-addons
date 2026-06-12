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
import java.util.UUID;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteAuthorizationConfigAdapter extends CoreEventHandlerAdapter {

  private final Logger logger = LoggerFactory.getLogger(RemoteAuthorizationConfigAdapter.class);
  private final OcppChargerSessionRegistry sessionRegistry;
  private final OcppSender sender;

  public RemoteAuthorizationConfigAdapter(OcppChargerSessionRegistry sessionRegistry, OcppSender sender) {
    this.sessionRegistry = sessionRegistry;
    this.sender = sender;
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex, BootNotificationRequest request) {
    ChargerReference reference = sessionRegistry.getCharger(sessionIndex);
    if (reference == null) {
      return null;
    }
    sender.send(reference, new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false"))
        .whenComplete((confirmation, ex) -> {
      if (ex != null) {
        logger.warn("ChangeConfiguration[AuthorizeRemoteTxRequests=false] for {} failed: {}", reference, ex.getMessage());
      } else {
        logger.debug("ChangeConfiguration[AuthorizeRemoteTxRequests=false] for {}: {}", reference, confirmation);
      }
    });
    return null;
  }
}
