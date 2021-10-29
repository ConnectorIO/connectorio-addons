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
import org.connectorio.addons.binding.askoheat.client.jackson.ActiveBooleanDeserializer;

public class AutoHeaterOffSettings {
  // "TRUE (60 seconds)"
  @JsonProperty("MODBUS_TIMEOUT")
  private String modbusTimeout;
  // "TRUE (60 seconds)"
  @JsonProperty("AUTO_RESTART")
  private String autoRestart;
  // "ACTIVE"
  @JsonProperty("MODBUS_CONTROL")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean modbusControl;
  // "ACTIVE"
  @JsonProperty("ANALOG_CONTROL")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean analogControl;
  // "ACTIVE"
  @JsonProperty("HEAT_PUMP_REQUEST")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean heatPumpRequest;
  // "ACTIVE"
  @JsonProperty("EMERGENCY_MODE")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean emergencyMode;
  // "1440 min."
  @JsonProperty("AUTO_OFF_COUNTDOWN_MINUTES")
  private String autoOffCountdownMinutes;
  // "1440 min."
  @JsonProperty("AUTO_HEATER_OFF_COUNTDOWN_VALUE")
  private String auto_heater_off_countdown_value;
  // "1209 min."
  @JsonProperty("EMERGENCY_OFF_COUNTDOWN_VALUE")
  private String emergency_off_countdown_value;
}
