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
package org.connectorio.addons.norule.internal.dispatch;

import org.connectorio.addons.norule.StateDispatcher;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPublisherStateDispatcher implements StateDispatcher {

  private final Logger logger = LoggerFactory.getLogger(EventPublisherStateDispatcher.class);
  private final EventPublisher eventPublisher;

  public EventPublisherStateDispatcher(EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void dispatch(Item item, State state) {
    ItemEvent event = ItemEventFactory.createStateEvent(item.getName(), state);
    logger.trace("Dispatching state event: {}, payload {}", event, event.getPayload());
    eventPublisher.post(event);
  }
}
