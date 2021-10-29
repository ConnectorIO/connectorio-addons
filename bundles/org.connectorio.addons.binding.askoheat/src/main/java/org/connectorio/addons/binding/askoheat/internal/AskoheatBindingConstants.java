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
package org.connectorio.addons.binding.askoheat.internal;

import org.connectorio.addons.binding.BaseBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

public interface AskoheatBindingConstants extends BaseBindingConstants {

  String BINDING_ID = BaseBindingConstants.identifier("askoheat");

  String ASKOHEAT_THING = "askoheat";

  ThingTypeUID ASKOHEAT_THING_TYPE = new ThingTypeUID(BINDING_ID, ASKOHEAT_THING);

  // five minutes
  Long DEFAULT_POLLING_INTERVAL = 300_000L;

}
