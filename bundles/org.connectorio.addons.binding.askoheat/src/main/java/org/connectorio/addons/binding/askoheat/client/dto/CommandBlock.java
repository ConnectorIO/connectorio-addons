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
public class CommandBlock {

  // "0" "3"
  @JsonProperty("MODBUS_CMD_SET_HEATER_STEP")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer step;

  // "-3000" (export), "400" (import)
  @JsonProperty("MODBUS_CMD_LOAD_FEEDIN_VALUE")
  @JsonSerialize(using = IntegerStringSerializer.class)
  @JsonDeserialize(using = IntegerStringDeserializer.class)
  private Integer gridPower;

  public Integer getStep() {
    return step;
  }

  public void setStep(Integer step) {
    this.step = step;
  }

  public Integer getGridPower() {
    return gridPower;
  }

  public void setGridPower(Integer gridPower) {
    this.gridPower = gridPower;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CommandBlock.class.getSimpleName() + "[", "]")
      .add("step=" + step)
      .add("gridPower=" + gridPower)
      .toString();
  }
}
