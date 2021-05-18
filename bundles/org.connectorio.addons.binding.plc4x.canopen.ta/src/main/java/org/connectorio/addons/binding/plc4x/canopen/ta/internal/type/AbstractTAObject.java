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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.type;

import java.util.Optional;
import org.apache.plc4x.java.canopen.readwrite.IndexAddress;

public class AbstractTAObject implements TAObject {

  protected final IndexAddress labelAddress;
  private final int index;
  private final int unit;
  private String label;

  public AbstractTAObject(int index, int unit, IndexAddress labelAddress) {
    this.index = index;
    this.unit = unit;
    this.labelAddress = labelAddress;
  }

  public IndexAddress getLabelAddress() {
    return labelAddress;
  }

  @Override
  public Optional<String> getLabel() {
    return Optional.ofNullable(label);
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public int getUnit() {
    return unit;
  }

  public void setLabel(String label) {
    this.label = label;
  }

}
