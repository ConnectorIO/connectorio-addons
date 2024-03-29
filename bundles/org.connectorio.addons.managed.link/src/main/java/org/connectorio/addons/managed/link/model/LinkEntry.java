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
package org.connectorio.addons.managed.link.model;

import java.util.Objects;

public class LinkEntry extends BaseLinkEntry {

  private String item;

  public void setItem(String item) {
    this.item = item;
  }

  public String getItem() {
    return item;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LinkEntry)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    LinkEntry linkEntry = (LinkEntry) o;
    return Objects.equals(getItem(), linkEntry.getItem());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getItem());
  }

}
