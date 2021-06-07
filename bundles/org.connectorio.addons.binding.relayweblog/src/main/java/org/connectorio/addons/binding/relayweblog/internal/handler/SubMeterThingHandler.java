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
package org.connectorio.addons.binding.relayweblog.internal.handler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants;
import org.connectorio.addons.binding.relayweblog.client.WeblogClient;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterReading;
import org.connectorio.addons.binding.relayweblog.client.dto.SubMeterReading;
import org.connectorio.addons.binding.relayweblog.internal.config.SubMeterConfig;
import org.openhab.core.thing.Thing;

public class SubMeterThingHandler extends AbstractMeterThingHandler<WeblogBridgeHandler, SubMeterConfig> {

  public SubMeterThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  protected Map<String, String> properties(Thing thing, List<MeterReading> readings) {
    Map<String, String> props = new LinkedHashMap<>(thing.getProperties());
    find(readings, RelayWeblogBindingConstants.ENHANCED_IDENTIFICATION_FIELD).ifPresent(val -> props.put(Thing.PROPERTY_SERIAL_NUMBER, val.getValue().trim()));
    find(readings, RelayWeblogBindingConstants.FABRICATION_IDENTIFIER_FIELD).ifPresent(val -> props.put(RelayWeblogBindingConstants.PROPERTY_PARENT_SERIAL_NUMBER, val.getValue().trim()));
    find(readings, "Model/Version").ifPresent(val -> props.put(Thing.PROPERTY_MODEL_ID, val.getValue().trim()));
    find(readings, "Hardware version number").ifPresent(val -> props.put(Thing.PROPERTY_HARDWARE_VERSION, val.getValue().trim()));
    find(readings, "Other software version number").ifPresent(val -> props.put(Thing.PROPERTY_FIRMWARE_VERSION, val.getValue().trim()));
    return props;
  }

  @Override
  protected List<MeterReading> readOut(WeblogClient client, SubMeterConfig config) {
    return client.getReadings(config.parentId);
  }

  @Override
  protected boolean acceptable(MeterReading reading) {
    return reading instanceof SubMeterReading && (config != null && ((SubMeterReading) reading).getSubMeterId().equals(config.id));
  }

}
