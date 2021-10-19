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
package org.connectorio.addons.norule.internal;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.connectorio.addons.norule.Trigger;
import org.connectorio.addons.norule.TriggerBuilder;
import org.connectorio.addons.norule.internal.trigger.MemberStateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.MemberStateUpdateTrigger;
import org.connectorio.addons.norule.internal.trigger.PeriodicTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerAddedTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerRemovedTrigger;
import org.connectorio.addons.norule.internal.trigger.ScheduledTrigger;
import org.connectorio.addons.norule.internal.trigger.StartLevelTrigger;
import org.connectorio.addons.norule.internal.trigger.StateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.StateUpdateTrigger;
import org.connectorio.addons.norule.internal.trigger.ThingStatusChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.ThingStatusTrigger;
import org.connectorio.chrono.Period;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

public class DefaultTriggerBuilder implements TriggerBuilder {

  private final Set<Trigger> triggers = new LinkedHashSet<>();

  @Override
  public TriggerBuilder memberStateChange(String group) {
    triggers.add(new MemberStateChangeTrigger(group));
    return this;
  }

  @Override
  public TriggerBuilder memberStateUpdate(String group) {
    triggers.add(new MemberStateUpdateTrigger(group));
    return this;
  }

  @Override
  public TriggerBuilder itemStateChange(String item) {
    triggers.add(new StateChangeTrigger(item));
    return this;
  }

  @Override
  public TriggerBuilder itemStateUpdate(String item) {
    triggers.add(new StateUpdateTrigger(item));
    return this;
  }

  @Override
  public TriggerBuilder schedule(long delay, TimeUnit unit) {
    triggers.add(new ScheduledTrigger(delay, unit));
    return this;
  }

  @Override
  public TriggerBuilder period(long delay, Period period) {
    triggers.add(new PeriodicTrigger(delay, period));
    return this;
  }

  @Override
  public TriggerBuilder startLevel(int level) {
    triggers.add(new StartLevelTrigger(level));
    return this;
  }

  @Override
  public TriggerBuilder markerAdded(ReadyMarker marker) {
    triggers.add(new ReadyMarkerAddedTrigger(marker));
    return this;
  }

  @Override
  public TriggerBuilder markerRemoved(ReadyMarker marker) {
    triggers.add(new ReadyMarkerRemovedTrigger(marker));
    return this;
  }

  @Override
  public TriggerBuilder thingStatus(ThingUID uid) {
    triggers.add(new ThingStatusTrigger((thing) -> uid.equals(thing.getUID())));
    return this;
  }

  @Override
  public TriggerBuilder thingStatus(ThingTypeUID type) {
    triggers.add(new ThingStatusTrigger((thing) -> type.equals(thing.getThingTypeUID())));
    return this;
  }

  @Override
  public TriggerBuilder thingStatus(Predicate<Thing> predicate) {
    triggers.add(new ThingStatusTrigger(predicate));
    return this;
  }

  @Override
  public TriggerBuilder thingStatusChange(ThingUID uid) {
    triggers.add(new ThingStatusChangeTrigger((thing) -> uid.equals(thing.getUID())));
    return this;
  }

  @Override
  public TriggerBuilder thingStatusChange(ThingTypeUID type) {
    triggers.add(new ThingStatusChangeTrigger((thing) -> type.equals(thing.getThingTypeUID())));
    return this;
  }

  @Override
  public TriggerBuilder thingStatusChange(Predicate<Thing> predicate) {
    triggers.add(new ThingStatusChangeTrigger(predicate));
    return this;
  }

  @Override
  public Set<Trigger> build() {
    return triggers;
  }
}
