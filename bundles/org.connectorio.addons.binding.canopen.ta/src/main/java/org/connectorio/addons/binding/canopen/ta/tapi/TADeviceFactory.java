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
package org.connectorio.addons.binding.canopen.ta.tapi;

import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.ta.internal.config.DeviceType;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TAEnergyMeter3Device;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TAIo45Device;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TARUvr610Device;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TARsm610Device;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TAUvr16x2Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TADeviceFactory {

  private final Logger logger = LoggerFactory.getLogger(TADeviceFactory.class);
  private final boolean identifyOnly;

  public TADeviceFactory() {
    this(false);
  }

  public TADeviceFactory(boolean identifyOnly) {
    this.identifyOnly = identifyOnly;
  }

  public CompletableFuture<TADevice> create(CoNode node, int clientId) {
    return node.<Short>read((short) 0x23E2, (short) 0x01, CANOpenDataType.UNSIGNED8)
      .thenApply(code -> new SimpleEntry<>(code, DeviceType.fromCode(code)))
      .thenApply(type -> get(type.getKey(), type.getValue(), node, clientId));
  }

  public TADevice get(DeviceType type, CoNode node, int clientId) {
    return get(0, type, node, clientId);
  }

  private TADevice get(int code, DeviceType type, CoNode node, int clientId) {
    if (DeviceType.UVR16x2 == type) {
      return new TAUvr16x2Device(node, clientId, identifyOnly);
    } else if (DeviceType.EZ3 == type) {
      return new TAEnergyMeter3Device(node, clientId, identifyOnly);
    } else if (DeviceType.RSM610 == type) {
      return new TARsm610Device(node, clientId, identifyOnly);
    } else if (DeviceType.IO45 == type) {
      return new TAIo45Device(node, clientId, identifyOnly);
    } else if (DeviceType.UVR610 == type) {
      return new TARUvr610Device(node, clientId, identifyOnly);
    } else if (DeviceType.SIMULATOR == type) {
      // virtual device
      return new TARsm610Device(node, clientId, identifyOnly);
    }

    throw new IllegalArgumentException("Unsupported device " + type + " " + code + " (0x" + Integer.toHexString(code) + ")");
  }

}
