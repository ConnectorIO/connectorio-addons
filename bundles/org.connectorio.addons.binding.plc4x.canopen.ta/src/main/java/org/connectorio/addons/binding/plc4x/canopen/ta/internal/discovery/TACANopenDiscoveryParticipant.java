/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.discovery;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.protocol.TAOperations;
import org.connectorio.addons.binding.plc4x.canopen.discovery.CANopenDiscoveryParticipant;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME Discovery participant running next to thing handler might interfere.
//@Component(service = {CANopenDiscoveryParticipant.class, DiscoveryService.class})
public class TACANopenDiscoveryParticipant extends AbstractDiscoveryService implements CANopenDiscoveryParticipant {

  private final Logger logger = LoggerFactory.getLogger(TACANopenDiscoveryParticipant.class);

  public TACANopenDiscoveryParticipant() {
    super(TACANopenBindingConstants.SUPPORTED_THINGS,30, true);
  }

  @Override
  public DiscoveryResult nodeDiscovered(PlcConnection connection, ThingUID bridgeUID, int node) {
    int clientId = 60;

    CompletableFuture<Map<String, String>> deviceInfo = new CompletableFuture<>();
    TAOperations operations = new TAOperations(connection);
    operations.subscribeStatus(loggedIn -> {
      if (loggedIn) {
        operations.identify(node).whenComplete((response, error) -> {
          if (error == null) {
            deviceInfo.complete(response);
          }
        });
      }
    }, node, clientId);

    operations.login(node, clientId);

    try {
      Map<String, String> info = deviceInfo.get(30, TimeUnit.SECONDS);
      DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(new ThingUID(
        TACANopenBindingConstants.TA_UVR_16x2_THING_TYPE, "" + node))
        .withBridge(bridgeUID)
        .withProperty("nodeId", node)
        .withRepresentationProperty("nodeId")
        .withProperty(Thing.PROPERTY_VENDOR, "Technische Alternative RT GmbH");

      Optional.ofNullable(info.get("name")).ifPresent(name -> resultBuilder.withLabel(name + " (TA UVR 16x2)"));
      Optional.ofNullable(info.get("function")).ifPresent(value -> resultBuilder.withProperty("function", cleanup(value)));
      Optional.ofNullable(info.get("version")).ifPresent(value -> resultBuilder.withProperty("version", cleanup(value)));
      Optional.ofNullable(info.get("serial")).ifPresent(value -> resultBuilder.withProperty(Thing.PROPERTY_SERIAL_NUMBER, cleanup(value)));
      Optional.ofNullable(info.get("production_date")).ifPresent(value -> resultBuilder.withProperty("production_date", cleanup(value)));
      Optional.ofNullable(info.get("bootsector")).ifPresent(value -> resultBuilder.withProperty("bootsector", cleanup(value)));
      Optional.ofNullable(info.get("hardware_cover")).ifPresent(value -> resultBuilder.withProperty("hardware_cover", cleanup(value)));
      Optional.ofNullable(info.get("hardware_mains")).ifPresent(value -> resultBuilder.withProperty("hardware_mains", cleanup(value)));

      return resultBuilder.build();
    } catch (InterruptedException | TimeoutException | ExecutionException e) {
      logger.debug("Could not complete device identification", e);
    } finally {
      operations.logout(node, clientId);
    }

    return null;
  }

  private String cleanup(String value) {
    return value.substring(value.indexOf(':') + 1).trim();
  }

  @Override
  protected void startScan() {

  }
}
