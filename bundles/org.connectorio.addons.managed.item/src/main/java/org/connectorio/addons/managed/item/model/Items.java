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
package org.connectorio.addons.managed.item.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Items {

  private List<ItemEntry> items;

  public Items() {
  }

  public Items(List<ItemEntry> items) {
    this.items = items;
  }

  public List<ItemEntry> getItems() {
    return items;
  }

  public void setItems(List<ItemEntry> items) {
    this.items = items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Items)) {
      return false;
    }
    Items items1 = (Items) o;
    return Objects.equals(items, items1.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items);
  }

}
