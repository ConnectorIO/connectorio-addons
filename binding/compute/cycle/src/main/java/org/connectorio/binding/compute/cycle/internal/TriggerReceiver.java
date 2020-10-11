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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.connectorio.binding.compute.cycle.internal.memo.StateReceiver;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

public class TriggerReceiver implements StateReceiver<ItemStateChangedEvent> {

  private final AtomicBoolean cycle = new AtomicBoolean(false);
  private final List<CycleOperation> operations = new CopyOnWriteArrayList<>();

  @Override
  public void accept(ItemStateChangedEvent itemStateChangedEvent) {
    BooleanType state = new BooleanType(itemStateChangedEvent.getItemState());

    boolean isCycleOpened = cycle.get();
    boolean callClose = isCycleOpened && !state.isOpen();
    boolean callOpen = !isCycleOpened && state.isOpen();

    if (callClose) {
      cycle.set(false);
    }

    if (callOpen) {
      cycle.set(true);
    }

    for (CycleOperation operation : operations) {
      /*
      if (operation instanceof StateReceiver) {
        ((StateReceiver<ItemStateChangedEvent>) operation).accept(itemStateChangedEvent);
      }
      */

      if (callOpen) {
        operation.open();
      }
      if (callClose) {
        operation.close();
      }
    }
  }

  public void addOperation(CycleOperation operation) {
    this.operations.add(operation);
  }

  public void removeOperation(ChannelUID channelUID) {
    List<CycleOperation> remove = this.operations.stream()
      .filter(op -> op.getChannelId().equals(channelUID))
      .collect(Collectors.toList());

    this.operations.removeAll(remove);
  }

  static class BooleanType {
    private final boolean state;

    BooleanType(State state) {
      if (state instanceof OnOffType) {
        this.state = state == OnOffType.ON;
      } else if (state instanceof OpenClosedType) {
        this.state = state == OpenClosedType.OPEN;
      } else {
        this.state = state.as(OnOffType.class) == OnOffType.ON;
      }
    }

    boolean isOpen() {
      return this.state;
    }
  }
}
