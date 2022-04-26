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
package org.connectorio.addons.binding.smartme;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.connectorio.addons.binding.BaseBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

public interface SmartMeBindingConstants extends BaseBindingConstants {

  String BINDING_ID = BaseBindingConstants.identifier("smartme");

  String SMARTME_CLOUD_BRIDGE = "cloud";
  String SMARTME_DEVICE_THING = "device";

  ThingTypeUID SMARTME_CLOUD_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, SMARTME_CLOUD_BRIDGE);
  ThingTypeUID SMARTME_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, SMARTME_DEVICE_THING);

  Set<ThingTypeUID> SUPPORTED_THINGS = Collections.unmodifiableSet(new HashSet<>(
    Arrays.asList(SMARTME_CLOUD_BRIDGE_TYPE, SMARTME_DEVICE_THING_TYPE)
  ));

  Long DEFAULT_REFRESH_INTERVAL = 300000L;
  String DEFAULT_CLOUD_URI = "https://smart-me.com:443";
}
