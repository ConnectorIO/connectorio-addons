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
package org.connectorio.addons.binding.ocpp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.connectorio.addons.binding.BaseBindingConstants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;

public interface OcppBindingConstants extends BaseBindingConstants {

  String BINDING_ID = BaseBindingConstants.identifier("ocpp");

  ThingTypeUID SERVER_THING_TYPE = new ThingTypeUID(BINDING_ID, "server");
  ThingTypeUID CHARGER_THING_TYPE = new ThingTypeUID(BINDING_ID, "charger");
  ThingTypeUID CONNECTOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "connector");

  Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(
    SERVER_THING_TYPE,
    CHARGER_THING_TYPE,
    CONNECTOR_THING_TYPE
  ));

  // common channel types
  ChannelRef CURRENT_EXPORT = new ChannelRef("currentExport");
  ChannelRef CURRENT_IMPORT = new ChannelRef("currentImport");
  ChannelRef CURRENT_OFFERED = new ChannelRef("currentOffered");
  ChannelRef ENERGY_ACTIVE_EXPORT = new ChannelRef("energyActiveExport");
  ChannelRef ENERGY_ACTIVE_IMPORT = new ChannelRef("energyActiveImport");
  ChannelRef ENERGY_REACTIVE_EXPORT = new ChannelRef("energyReactiveExport");
  ChannelRef ENERGY_REACTIVE_IMPORT = new ChannelRef("energyReactiveImport");
  ChannelRef ENERGY_ACTIVE_EXPORT_INTERVAL = new ChannelRef("energyActiveExportInterval");
  ChannelRef ENERGY_ACTIVE_IMPORT_INTERVAL = new ChannelRef("energyActiveImportInterval");
  ChannelRef ENERGY_REACTIVE_EXPORT_INTERVAL = new ChannelRef("energyReactiveExportInterval");
  ChannelRef ENERGY_REACTIVE_IMPORT_INTERVAL = new ChannelRef("energyReactiveImportInterval");
  ChannelRef FREQUENCY = new ChannelRef("frequency");
  ChannelRef POWER_ACTIVE_EXPORT = new ChannelRef("powerActiveExport");
  ChannelRef POWER_ACTIVE_IMPORT = new ChannelRef("powerActiveImport");
  ChannelRef POWER_FACTOR = new ChannelRef("powerFactor");
  ChannelRef POWER_OFFERED = new ChannelRef("powerOffered");
  ChannelRef POWER_REACTIVE_EXPORT = new ChannelRef("powerReactiveExport");
  ChannelRef POWER_REACTIVE_IMPORT = new ChannelRef("powerReactiveImport");
  ChannelRef RPM = new ChannelRef("rpm");
  ChannelRef SOC = new ChannelRef("soc");
  ChannelRef TEMPERATURE = new ChannelRef("temperature");
  ChannelRef VOLTAGE = new ChannelRef("voltage");

  static class ChannelRef extends UID {

    public ChannelRef(String uid) {
      super(uid);
    }

    @Override
    protected int getMinimalNumberOfSegments() {
      return 1;
    }
  }

}
