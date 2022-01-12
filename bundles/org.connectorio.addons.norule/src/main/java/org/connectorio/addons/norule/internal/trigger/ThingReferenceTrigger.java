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
package org.connectorio.addons.norule.internal.trigger;

import java.util.function.Predicate;
import org.connectorio.addons.norule.Trigger;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

public abstract class ThingReferenceTrigger implements Trigger {

  private final Predicate<ThingUID> predicate;

  public ThingReferenceTrigger(Predicate<ThingUID> predicate) {
    this.predicate = predicate;
  }

  public Predicate<ThingUID> getPredicate() {
    return predicate;
  }

  public String toString() {
    return "(thing trigger)";
  }
}
