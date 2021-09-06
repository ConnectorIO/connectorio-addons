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
package org.connectorio.addons.managed.item.internal;

import java.util.Collection;
import java.util.Set;
import org.openhab.core.common.registry.AbstractProvider;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemProvider;

public class XStreamItemProvider extends AbstractProvider<Item> implements ItemProvider {

  private final Set<Item> items;

  public XStreamItemProvider(Set<Item> items) {
    this.items = items;
  }


  @Override
  public Collection<Item> getAll() {
    return items;
  }

}