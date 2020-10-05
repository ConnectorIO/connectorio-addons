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
package org.connectorio.binding.compute.cycle.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link CycleBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface CycleBindingConstants {

  String BINDING_ID = "co7io-compute-cycle";

  ThingTypeUID THING_TYPE_CYCLE_COUNTER = new ThingTypeUID(BINDING_ID, "cycle-counter");

  String TIME = "time";
  String COUNT = "count";
  String DIFFERENCE = "difference";

  ChannelTypeUID DIFFERENCE_TYPE = new ChannelTypeUID(BINDING_ID, DIFFERENCE);

}