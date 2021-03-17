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

import java.util.function.Consumer;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.TABaseObjectThingHandler;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.TADeviceThingHandler;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider.TAChannelTypeProvider;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.InOutCallback;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalOutput;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryThingHandlerService extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService,
  Consumer<TACanInputOutputObject<?>>, InOutCallback {

  private final Logger logger = LoggerFactory.getLogger(DiscoveryThingHandlerService.class);

  private TADeviceThingHandler handler;
  private TADevice device;

  public DiscoveryThingHandlerService() {
    super(TACANopenBindingConstants.DISCOVERABLE_CAN_THINGS, 60);
  }

  @Override
  protected void startScan() {

  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof TADeviceThingHandler) {
      logger.debug("Attaching discovery service to handler {}", handler);
      this.handler = (TADeviceThingHandler) handler;
      this.handler.getDevice().whenComplete((result, error) -> {
        this.device = result;
        if (result != null) {
          logger.debug("Registration of in/out discovery callback for device {}", device);
          result.addInOutCallback(this);
        }
      });
    }
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  public ThingHandler getThingHandler() {
    return handler;
  }

  @Override
  public void activate() {

  }

  @Override
  public void deactivate() {
    if (device != null) {
      device.removeInOutCallback(this);
    }
  }

  @Override
  public void accept(TACanInputOutputObject<?> object) {
    logger.info("Discovered new device level I/O object {}", object);

    // TODO correlate inputs and outputs maybe?
    if (object instanceof TAAnalogOutput) {
      TAAnalogOutput analogOutput = (TAAnalogOutput) object;

      AnalogUnit unit = AnalogUnit.valueOf(analogOutput.getUnit());
      ThingTypeUID type = TAChannelTypeProvider.forUnit(unit);
      if (unit == null || type == null) {
        logger.info("Ignoring I/O object {} object. Could not find matching channel and thing for unit {}", object, unit);
        return;
      }

      ThingUID bridgeUID = handler.getThing().getUID();
      analogOutput.getName().whenComplete((name, error) -> {
        if (error != null) {
          logger.info("Failed to read analog label", error);
          name = "Analog object #" + analogOutput.getIndex();
        }

        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(new ThingUID(type, bridgeUID, "" + object.getIndex()))
          .withBridge(bridgeUID)
          .withLabel(name)
          .withProperty("id", analogOutput.getIndex())
          .withProperty(TABaseObjectThingHandler.OUTPUT, analogOutput.getIndex())
          .withProperty("unit", unit.name())
          .withRepresentationProperty("id");
        thingDiscovered(resultBuilder.build());
      });
    }
    if (object instanceof TADigitalOutput) {
      TADigitalOutput digitalOutput = (TADigitalOutput) object;

      DigitalUnit unit = DigitalUnit.valueOf(digitalOutput.getUnit());
      ThingTypeUID type = TAChannelTypeProvider.forUnit(unit);
      if (unit == null || type == null) {
        logger.info("Ignoring I/O object {} object. Could not find matching channel and thing for unit {}", object, unit);
        return;
      }

      ThingUID bridgeUID = handler.getThing().getUID();
      digitalOutput.getName().whenComplete((name, error) -> {
        if (error != null) {
          logger.info("Failed to read analog label", error);
          name = "Digital object #" + digitalOutput.getIndex();
        }
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(new ThingUID(type, bridgeUID, "" + object.getIndex()))
          .withBridge(bridgeUID)
          .withLabel(name)
          .withProperty("id", digitalOutput.getIndex())
          .withProperty(TABaseObjectThingHandler.OUTPUT, digitalOutput.getIndex())
          .withProperty("unit", unit.name())
          .withRepresentationProperty("id");
        thingDiscovered(resultBuilder.build());
      });
    }
  }

}
