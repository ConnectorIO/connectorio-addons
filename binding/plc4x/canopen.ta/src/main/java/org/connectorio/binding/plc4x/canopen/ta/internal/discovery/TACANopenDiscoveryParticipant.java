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
package org.connectorio.binding.plc4x.canopen.ta.internal.discovery;

import static org.connectorio.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants.TA_UVR_16x2_THING_TYPE;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.connectorio.binding.plc4x.canopen.discovery.CANopenDiscoveryParticipant;
import org.connectorio.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {CANopenDiscoveryParticipant.class, DiscoveryService.class})
public class TACANopenDiscoveryParticipant extends AbstractDiscoveryService implements CANopenDiscoveryParticipant {

  private final Logger logger = LoggerFactory.getLogger(TACANopenDiscoveryParticipant.class);

  public TACANopenDiscoveryParticipant() {
    super(TACANopenBindingConstants.SUPPORTED_THINGS,30, true);
  }

  @Override
  public DiscoveryResult nodeDiscovered(PlcConnection connection, ThingUID bridgeUID, int node) {
    try {
      final PlcReadResponse response = connection.readRequestBuilder().addItem("type", "SDO:" + node + ":0x23E2/0x01:UNSIGNED8")
        .build().execute().get(5, TimeUnit.SECONDS);

      final Integer type = response.getInteger("type");
      if (0x87 == type) {
        final PlcReadResponse readResponse = connection.readRequestBuilder()
          .addItem("name", "SDO:" + node + ":0x2512/0x00:VISIBLE_STRING")
          .addItem("function", "SDO:" + node + ":0x57E0/0x07:VISIBLE_STRING")
          .addItem("version", "SDO:" + node + ":0x57E0/0x00:VISIBLE_STRING")
          .addItem("serial", "SDO:" + node + ":0x57E0/0x01:VISIBLE_STRING")
          .addItem("production_date", "SDO:" + node + ":0x57E0/0x02:VISIBLE_STRING")
          .addItem("bootsector", "SDO:" + node + ":0x57E0/0x03:VISIBLE_STRING")
          .addItem("hardware_cover", "SDO:" + node + ":0x57E0/0x04:VISIBLE_STRING")
          .addItem("hardware_mains", "SDO:" + node + ":0x57E0/0x05:VISIBLE_STRING")
          .build().execute().get(10, TimeUnit.SECONDS);

        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(new ThingUID(TA_UVR_16x2_THING_TYPE, "" + node))
          .withBridge(bridgeUID)
          .withProperty("nodeId", node)
          .withProperty(Thing.PROPERTY_VENDOR, "Technische Alternative RT GmbH");

        Optional.ofNullable(readResponse.getString("name")).ifPresent(name -> resultBuilder.withLabel(name + " (TA UVR 16x2)"));
        Optional.ofNullable(readResponse.getString("function")).ifPresent(value -> resultBuilder.withProperty("function", cleanup(value)));
        Optional.ofNullable(readResponse.getString("version")).ifPresent(value -> resultBuilder.withProperty("version", cleanup(value)));
        Optional.ofNullable(readResponse.getString("serial")).ifPresent(value -> resultBuilder.withProperty(Thing.PROPERTY_SERIAL_NUMBER, cleanup(value)));
        Optional.ofNullable(readResponse.getString("production_date")).ifPresent(value -> resultBuilder.withProperty("production_date", cleanup(value)));
        Optional.ofNullable(readResponse.getString("bootsector")).ifPresent(value -> resultBuilder.withProperty("bootsector", cleanup(value)));
        Optional.ofNullable(readResponse.getString("hardware_cover")).ifPresent(value -> resultBuilder.withProperty("hardware_cover", cleanup(value)));
        Optional.ofNullable(readResponse.getString("hardware_mains")).ifPresent(value -> resultBuilder.withProperty("hardware_mains", cleanup(value)));

        return resultBuilder.build();
      }
    } catch (InterruptedException | ExecutionException e) {
      logger.debug("Failed to discover device information", e);
    } catch (TimeoutException e) {
      logger.debug("Device identification request failed", e);
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
