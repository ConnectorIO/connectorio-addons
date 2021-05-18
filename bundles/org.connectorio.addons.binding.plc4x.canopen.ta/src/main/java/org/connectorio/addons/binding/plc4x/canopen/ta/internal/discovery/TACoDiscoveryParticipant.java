/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.discovery.CoDiscoveryParticipant;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TADeviceFactory;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.plc4x.decorator.phase.Phase;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME Discovery participant running next to thing handler might interfere.
//@Component(service = {CoDiscoveryParticipant.class, DiscoveryService.class})
public class TACoDiscoveryParticipant extends AbstractDiscoveryService implements CoDiscoveryParticipant {

  private final Logger logger = LoggerFactory.getLogger(TACoDiscoveryParticipant.class);

  public TACoDiscoveryParticipant() {
    super(TACANopenBindingConstants.SUPPORTED_DEVICES,30, true);
  }

  @Override
  public DiscoveryResult nodeDiscovered(ThingUID bridgeUID, CoNode node) {
    Phase phase = new Phase("discover-ta-device-" + node.getNodeId());
    try {
      logger.info("Trying to identify node {} as Technische Alternative device.", node);
      ThingUID thingUID = new ThingUID(TACANopenBindingConstants.TA_DEVICE_THING_TYPE, bridgeUID, String.valueOf(node.getNodeId()));
      return new TADeviceFactory(true).create(node, node.getConnection().getLocalNode().getNodeId())
        .thenApply(new DiscoveryFunction(phase, thingUID))
        .get(60_000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      logger.warn("Failed to scan device information", e);
    } finally {
      logger.warn("Completed phase initialization {}", phase);
    }
    return null;
  }

  @Override
  protected void startScan() {

  }

  static class DiscoveryFunction implements Function<TADevice, DiscoveryResult>, Consumer<Boolean>, Runnable {

    private final Phase phase;
    private final ThingUID thingUID;
    private final CompletableFuture<DiscoveryResult> result = new CompletableFuture<>();
    private TADevice device;

    DiscoveryFunction(Phase phase, ThingUID thingUID) {
      this.phase = phase;
      this.thingUID = thingUID;
      phase.onCompletion(this);
    }

    @Override
    public DiscoveryResult apply(TADevice response) {
      this.device = response;
      response.addStatusCallback(this);
      response.login();
      return result.join();
    }

    @Override
    public void accept(Boolean loggedIn) {
      if (loggedIn) {
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(thingUID);
        device.getName().thenApply(value -> resultBuilder.withLabel(value + " (TA device)"));
        device.getFunction().thenApply(value -> resultBuilder.withProperty("function", cleanup(value)));
        device.getVersion().thenApply(value -> resultBuilder.withProperty("version", cleanup(value)));
        device.getSerial().thenApply(value -> resultBuilder.withProperty(Thing.PROPERTY_SERIAL_NUMBER, cleanup(value)));
        device.getProductionDate().thenApply(value -> resultBuilder.withProperty("production_date", cleanup(value)));
        device.getBootsector().thenApply(value -> resultBuilder.withProperty("bootsector", cleanup(value)));
        device.getHardwareCover().thenApply(value -> resultBuilder.withProperty("hardware_cover", cleanup(value)));
        device.getHardwareMains().thenApply(value -> resultBuilder.withProperty("hardware_mains", cleanup(value)));
        result.complete(resultBuilder.build());
        return;
      }

      result.completeExceptionally(new IllegalStateException("Login attempt failed"));
    }

    private static String cleanup(String value) {
      return value.substring(value.indexOf(':') + 1).trim();
    }

    @Override
    public void run() {
      if (device != null) {
        device.logout();
      }
    }
  }
}
