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
package org.connectorio.addons.norule.internal.context;

import java.util.Optional;
import org.connectorio.addons.norule.ItemContext;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.types.State;

public class StateChangeItemContext implements ItemContext {

  private final Item item;
  private final State state;

  public StateChangeItemContext(Item item, State state) {
    this.item = item;
    this.state = state;
  }

  @Override
  public Optional<State> state() {
    return Optional.of(state);
  }

  @Override
  public <X extends State> Optional<X> state(Class<X> type) {
    return Optional.ofNullable(state.as(type));
  }

  @Override
  public void state(State state) {
    if (item instanceof GenericItem) {
      ((GenericItem) item).setState(state);
    }
  }
}
