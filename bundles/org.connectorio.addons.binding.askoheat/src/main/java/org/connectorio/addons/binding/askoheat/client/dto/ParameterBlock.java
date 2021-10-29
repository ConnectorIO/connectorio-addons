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

// GETPAR.json
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterBlock {

  // "650"
  @JsonProperty("MODBUS_PAR_HEATER1_POWER")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer heater1;
  // "1250"
  @JsonProperty("MODBUS_PAR_HEATER2_POWER")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer heater2;
  // "2500"
  @JsonProperty("MODBUS_PAR_HEATER3_POWER")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer heater3;

  public Integer getHeater1() {
    return heater1;
  }

  public void setHeater1(Integer heater1) {
    this.heater1 = heater1;
  }

  public Integer getHeater2() {
    return heater2;
  }

  public void setHeater2(Integer heater2) {
    this.heater2 = heater2;
  }

  public Integer getHeater3() {
    return heater3;
  }

  public void setHeater3(Integer heater3) {
    this.heater3 = heater3;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ParameterBlock.class.getSimpleName() + "[", "]")
      .add("heater1=" + heater1)
      .add("heater2=" + heater2)
      .add("heater3=" + heater3)
      .toString();
  }
}
