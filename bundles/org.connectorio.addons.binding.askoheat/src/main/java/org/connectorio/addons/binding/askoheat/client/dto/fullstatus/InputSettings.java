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
package org.connectorio.addons.binding.askoheat.client.dto.fullstatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.StringJoiner;
import org.connectorio.addons.binding.askoheat.client.jackson.ActiveBooleanDeserializer;

public class InputSettings {
  // "ACTIVE"
  @JsonProperty("MISSING_CURRENT_FLOW_TRIGGERS_ERROR")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean missingCurrentFlowTriggersError;
  // "ACTIVE"
  @JsonProperty("HEATER_LOAD_ONLY_IF_CURRENT_FLOWS")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean heaterLoadOnlyIfCurrentFlows;
  // "ACTIVE"
  @JsonProperty("INPUT_SET_HEATER_STEP")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean inputSetHeaterStep;
  // "ACTIVE"
  @JsonProperty("INPUT_LOAD_SETPOINT")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean inputLoadSetpoint;
  // "ACTIVE"
  @JsonProperty("INPUT_LOAD_FEEDIN")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean inputLoadFeedin;
  // "ACTIVE"
  @JsonProperty("INPUT_ANALOG_CONTROL")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean inputAnalogControl;
  // "ACTIVE"
  @JsonProperty("INPUT_HEAT_PUMP_REQUEST")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean inputHeatPumpRequest;
  // "ACTIVE"
  @JsonProperty("INPUT_EMERGENCY_MODE")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean inputEmergencyMode;
  // "NOT ACTIVE"
  @JsonProperty("USE_LOW_TARIFF")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useLowTariff;
  // "NOT ACTIVE"
  @JsonProperty("USE_MINIMAL_TEMPERATURE")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useMinimalTemperature;
  // "NOT ACTIVE"
  @JsonProperty("USE_SPECIAL_SMA_SEMP")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useSpecialSmaSemp;
  // "NOT ACTIVE"
  @JsonProperty("USE_SPECIAL_SENEC_HOME")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useSpecialSenecHome;

  @Override
  public String toString() {
    return new StringJoiner(", ", InputSettings.class.getSimpleName() + "[", "]")
      .add("missingCurrentFlowTriggersError=" + missingCurrentFlowTriggersError)
      .add("heaterLoadOnlyIfCurrentFlows=" + heaterLoadOnlyIfCurrentFlows)
      .add("inputSetHeaterStep=" + inputSetHeaterStep)
      .add("inputLoadSetpoint=" + inputLoadSetpoint)
      .add("inputLoadFeedin=" + inputLoadFeedin)
      .add("inputAnalogControl=" + inputAnalogControl)
      .add("inputHeatPumpRequest=" + inputHeatPumpRequest)
      .add("inputEmergencyMode=" + inputEmergencyMode)
      .add("useLowTariff=" + useLowTariff)
      .add("useMinimalTemperature=" + useMinimalTemperature)
      .add("useSpecialSmaSemp=" + useSpecialSmaSemp)
      .add("useSpecialSenecHome=" + useSpecialSenecHome)
      .toString();
  }
}
