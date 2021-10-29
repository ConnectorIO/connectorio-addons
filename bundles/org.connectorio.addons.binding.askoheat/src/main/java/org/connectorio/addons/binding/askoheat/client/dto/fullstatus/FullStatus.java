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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FullStatus {

  @JsonProperty("ASKOHEAT_PLUS_INFO")
  private DeviceInfo deviceInfo;

  @JsonProperty("ERROR_FLAGS")
  private Errors errors;

  @JsonProperty("LEGIONELLA_PROTECTION_STATUS")
  private LegionellaStatus legionellaStatus;

  @JsonProperty("LEGIONELLA_PROTECTION_SETTINGS")
  private LegionellaSettings legionellaSettings;

  @JsonProperty("INPUT_SETTINGS")
  private InputSettings inputSettings;

  @JsonProperty("TEMPERATURE_SETTINGS")
  private TemperatureSettings temperatureSettings;

  @JsonProperty("AUTO_HEATER_OFF_SETTINGS")
  private AutoHeaterOffSettings autoHeaterOffSettings;

  public DeviceInfo getDeviceInfo() {
    return deviceInfo;
  }

  public Errors getErrors() {
    return errors;
  }

  public LegionellaStatus getLegionellaStatus() {
    return legionellaStatus;
  }

  public LegionellaSettings getLegionellaSettings() {
    return legionellaSettings;
  }

  public InputSettings getInputSettings() {
    return inputSettings;
  }

  public TemperatureSettings getTemperatureSettings() {
    return temperatureSettings;
  }

  public AutoHeaterOffSettings getAutoHeaterOffSettings() {
    return autoHeaterOffSettings;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FullStatus.class.getSimpleName() + "[", "]")
      .add("deviceInfo=" + deviceInfo)
      .add("errors=" + errors)
      .add("legionellaStatus=" + legionellaStatus)
      .add("legionellaSettings=" + legionellaSettings)
      .add("inputSettings=" + inputSettings)
      .add("temperatureSettings=" + temperatureSettings)
      .toString();
  }
}
