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
package org.connectorio.addons.compute.consumption.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ConsumptionBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface ConsumptionBindingConstants {

  String BINDING_ID = "co7io-compute-consumption";

  ThingTypeUID THING_TYPE_CONSUMPTION = new ThingTypeUID(BINDING_ID, "consumption");

  // List of all Channel kinds computed by handler (s)
  String ONE_MINUTE = "oneMinute";
  String FIVE_MINUTES = "fiveMinutes";
  String FIFTEEN_MINUTES = "fifteenMinutes";
  String THIRTY_MINUTES = "thirtyMinutes";
  String HOURLY = "hourly";
  String DAILY = "daily";
  String MONTHLY = "monthly";

}
