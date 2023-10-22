/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.amsads.internal.symbol;

import java.util.Objects;
import org.apache.plc4x.java.ads.readwrite.AdsDataType;
import org.apache.plc4x.java.ads.readwrite.PlcValueType;

public class SymbolEntry {

  private final PlcValueType type;
  private final String name;
  private final String description;
  private final long index;
  private final long offset;
  private final boolean readOnly;

  public SymbolEntry(PlcValueType type, String name, String description, long index, long offset, boolean readOnly) {
    this.type = type;
    this.name = name;
    this.description = description;
    this.index = index;
    this.offset = offset;
    this.readOnly = readOnly;
  }

  public PlcValueType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public long getIndex() {
    return index;
  }

  public long getOffset() {
    return offset;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SymbolEntry)) {
      return false;
    }
    SymbolEntry that = (SymbolEntry) o;
    return getIndex() == that.getIndex() && getOffset() == that.getOffset()
      && isReadOnly() == that.isReadOnly() && getType() == that.getType()
      && Objects.equals(getName(), that.getName()) && Objects.equals(
      getDescription(), that.getDescription());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), getName(), getDescription(), getIndex(), getOffset(),
        isReadOnly());
  }

}