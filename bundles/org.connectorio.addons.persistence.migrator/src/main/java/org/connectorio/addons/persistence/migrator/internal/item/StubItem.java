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
package org.connectorio.addons.persistence.migrator.internal.item;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;

public class StubItem implements Item {

    private final Item item;
    private final State state;

    public StubItem(Item item, State state) {
      this.item = item;
      this.state = state;
    }

    @Override
    public State getState() {
      return state;
    }

    @Override
    public <T extends State> T getStateAs(Class<T> typeClass) {
      return state.as(typeClass);
    }

    @Override
    public String getName() {
      return item.getName();
    }

    @Override
    public String getType() {
      return item.getType();
    }

    @Override
    public List<Class<? extends State>> getAcceptedDataTypes() {
      return item.getAcceptedDataTypes();
    }

    @Override
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
      return item.getAcceptedCommandTypes();
    }

    @Override
    public List<String> getGroupNames() {
      return item.getGroupNames();
    }

    @Override
    public Set<String> getTags() {
      return item.getTags();
    }

    @Override
    public String getLabel() {
      return item.getLabel();
    }

    @Override
    public boolean hasTag(String tag) {
      return item.hasTag(tag);
    }

    @Override
    public String getCategory() {
      return item.getCategory();
    }

    @Override
    public StateDescription getStateDescription() {
      return item.getStateDescription();
    }

    @Override
    public StateDescription getStateDescription(Locale locale) {
      return item.getStateDescription(locale);
    }

    @Override
    public CommandDescription getCommandDescription() {
      return item.getCommandDescription();
    }

    @Override
    public CommandDescription getCommandDescription(Locale locale) {
      return item.getCommandDescription(locale);
    }

    @Override
    public String getUID() {
      return item.getUID();
    }

    @Override
    public State getLastState() {
      return item.getLastState();
    }

    @Override
    public ZonedDateTime getLastStateChange() {
      return item.getLastStateChange();
    }

    @Override
    public ZonedDateTime getLastStateUpdate() {
      return item.getLastStateUpdate();
    }

}
