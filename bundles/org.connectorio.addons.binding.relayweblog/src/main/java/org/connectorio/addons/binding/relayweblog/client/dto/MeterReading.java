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
package org.connectorio.addons.binding.relayweblog.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class MeterReading {

  private final String name; // "Energy"
  private final String value; // "99153"
  private final String unit; // "kWh "

  @JsonCreator
  public MeterReading(@JsonProperty("Name") String name, @JsonProperty("Value") String value, @JsonProperty("Unit") String unit) {
    this.name = name.trim();
    this.value = value.trim();
    this.unit = unit.trim();
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getUnit() {
    return unit;
  }

  public String toString() {
    return "Meter Reading [" + name + "=" + value + (unit.isEmpty() ? "" : " (" + unit + ")") + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MeterReading)) {
      return false;
    }
    MeterReading reading = (MeterReading) o;
    return Objects.equals(getName(), reading.getName()) && Objects.equals(getValue(), reading.getValue())
      && Objects.equals(getUnit(), reading.getUnit());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getValue(), getUnit());
  }
}
