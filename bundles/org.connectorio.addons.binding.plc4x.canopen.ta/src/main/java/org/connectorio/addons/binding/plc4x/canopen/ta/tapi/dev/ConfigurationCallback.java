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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev;

import org.apache.plc4x.java.canopen.readwrite.IndexAddress;
import org.apache.plc4x.java.canopen.readwrite.io.IndexAddressIO;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;

class ConfigurationCallback extends AbstractCallback {

  private final TADevice device;
  private final ObjectFactory objectFactory;

  public ConfigurationCallback(TADevice device, ObjectFactory objectFactory) {
    this.device = device;
    this.objectFactory = objectFactory;
  }

  @Override
  protected void accept(ReadBuffer buffer) throws ParseException {
    int sender = buffer.readUnsignedShort(8);
    int nodeId = device.getNode().getNodeId();
    if (sender != nodeId) {
      logger.warn("Received configuration notification from wrong node: {}. Configured node id {}", sender, nodeId);
      return;
    }
    logger.info("Received configuration of device {} outputs. Payload {}", device, buffer);

    IndexAddress address = IndexAddressIO.staticParse(buffer);
    final int subIndex = address.getSubindex();
    short rawValue = buffer.readShort(16);
    buffer.readByte(8); // constant 0x41
    int unit = buffer.readUnsignedShort(8);

    if (logger.isDebugEnabled()) {
      logger.debug("IO configuration from node: {}. Sub index: {}, raw {}, unit {}.", sender,
        subIndex, Integer.toHexString(rawValue), unit);
    }

    if (subIndex <= 32) { // analog
      device.discoverAnalogOutput(objectFactory.createAnalogOutput(subIndex, unit, rawValue));
    } else if (subIndex <= 64) { // digital
      device.discoverDigitalOutput(objectFactory.createDigitalOutput(subIndex - 32, unit, rawValue != 0));
    } else {
      logger.warn("Value {} out of range", subIndex);
    }
  }

}