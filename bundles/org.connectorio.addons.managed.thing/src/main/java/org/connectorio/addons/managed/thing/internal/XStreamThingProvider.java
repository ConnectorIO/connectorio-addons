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
package org.connectorio.addons.managed.thing.internal;

import java.util.Collection;
import java.util.Set;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;

public class XStreamThingProvider implements ThingProvider {

  private final Set<Thing> things;

  public XStreamThingProvider(Set<Thing> things) {
    this.things = things;
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<Thing> listener) {

  }

  @Override
  public Collection<Thing> getAll() {
    return things;
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<Thing> listener) {

  }
}
