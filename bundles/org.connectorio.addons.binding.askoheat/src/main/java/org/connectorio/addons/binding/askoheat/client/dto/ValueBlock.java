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
package org.connectorio.addons.binding.askoheat.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.StringJoiner;
import org.connectorio.addons.binding.askoheat.client.jackson.IntegerStringDeserializer;
import org.connectorio.addons.binding.askoheat.client.jackson.IntegerStringSerializer;

// GETVAL.json
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueBlock {

  // "0"
  @JsonProperty("MODBUS_VAL_STATUS")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer status;

  // "4400"
  @JsonProperty("MODBUS_VAL_HEATER_LOAD")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer load;

  // "54"
  @JsonProperty("MODBUS_VAL_TEMPERATURE_SENSOR0")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer temperatureSensor0;

  // "90"
  @JsonProperty("MODBUS_VAL_ACTUAL_TEMPERATURE_LIMIT")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer temperatureLimit;

  // "55"
  @JsonProperty("MODBUS_VAL_MAX_TEMPERATURE")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer maximumTemperature;

  public boolean isEmergencyMode() {
    return (status & 0x80) == 0x80;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getLoad() {
    return load;
  }

  public void setLoad(Integer load) {
    this.load = load;
  }

  public Integer getTemperatureSensor0() {
    return temperatureSensor0;
  }

  public void setTemperatureSensor0(Integer temperatureSensor0) {
    this.temperatureSensor0 = temperatureSensor0;
  }

  public Integer getTemperatureLimit() {
    return temperatureLimit;
  }

  public void setTemperatureLimit(Integer temperatureLimit) {
    this.temperatureLimit = temperatureLimit;
  }

  public Integer getMaximumTemperature() {
    return maximumTemperature;
  }

  public void setMaximumTemperature(Integer maximumTemperature) {
    this.maximumTemperature = maximumTemperature;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ValueBlock.class.getSimpleName() + "[", "]")
      .add("status=" + status)
      .add("load=" + load)
      .add("temperatureSensor0=" + temperatureSensor0)
      .add("temperatureLimit=" + temperatureLimit)
      .add("maximumTemperature=" + maximumTemperature)
      .add("emergencyMode=" + isEmergencyMode())
      .toString();
  }
}
