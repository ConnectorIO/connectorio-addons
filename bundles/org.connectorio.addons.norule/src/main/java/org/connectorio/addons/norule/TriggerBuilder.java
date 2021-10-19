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
package org.connectorio.addons.norule;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.connectorio.chrono.Period;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

public interface TriggerBuilder {

  TriggerBuilder memberStateChange(String group);

  TriggerBuilder memberStateUpdate(String group);

  TriggerBuilder itemStateChange(String item);

  TriggerBuilder itemStateUpdate(String item);

  TriggerBuilder startLevel(int level);

  TriggerBuilder markerAdded(ReadyMarker marker);
  TriggerBuilder markerRemoved(ReadyMarker marker);

  TriggerBuilder schedule(long delay, TimeUnit unit);

  TriggerBuilder period(long delay, Period period);

  TriggerBuilder thingStatus(ThingUID thing);
  TriggerBuilder thingStatus(ThingTypeUID thing);
  TriggerBuilder thingStatus(Predicate<Thing> predicate);

  TriggerBuilder thingStatusChange(ThingUID thing);
  TriggerBuilder thingStatusChange(ThingTypeUID thing);
  TriggerBuilder thingStatusChange(Predicate<Thing> predicate);


  Set<Trigger> build();

}
