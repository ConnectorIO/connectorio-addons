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
import org.connectorio.addons.binding.askoheat.client.jackson.TextBooleanDeserializer;

public class LegionellaStatus {
  // "DISABLED"
  @JsonProperty("STATUS_INFO")
  private String statusInfo;
  // "FALSE"
  @JsonProperty("TEMP_REACHED_OUTSIDE_PERIOD")
  @JsonDeserialize(using = TextBooleanDeserializer.class)
  private boolean temperatureReachedOutsidePeriod;
  // "FALSE"
  @JsonProperty("WRONG_SETTINGS")
  @JsonDeserialize(using = TextBooleanDeserializer.class)
  private boolean wrongSettings;
  // "FALSE"
  @JsonProperty("TEMP_NOT_REACHED_WITHIN")
  @JsonDeserialize(using = TextBooleanDeserializer.class)
  private boolean temperatureMotReachedWithin;
  // "FALSE"
  @JsonProperty("NO_VALID_TEMP_SENSOR_CONNECTED")
  @JsonDeserialize(using = TextBooleanDeserializer.class)
  private boolean noValidTemperatureSensorConnected;
  // "FALSE"
  @JsonProperty("LOAD_LOST_DURING_HEATING_UP")
  @JsonDeserialize(using = TextBooleanDeserializer.class)
  private boolean loadLostDuringHeatingUp;
  // "0 day 0 hour 0 min."
  @JsonProperty("LAST_LEGIONELLA_PROTECTION_BEFORE")
  private String lastLegionellaProtectionBefore;

  @Override
  public String toString() {
    return new StringJoiner(", ", LegionellaStatus.class.getSimpleName() + "[", "]")
      .add("statusInfo='" + statusInfo + "'")
      .add("temperatureReachedOutsidePeriod=" + temperatureReachedOutsidePeriod)
      .add("wrongSettings=" + wrongSettings)
      .add("temperatureMotReachedWithin=" + temperatureMotReachedWithin)
      .add("noValidTemperatureSensorConnected=" + noValidTemperatureSensorConnected)
      .add("loadLostDuringHeatingUp=" + loadLostDuringHeatingUp)
      .add("lastLegionellaProtectionBefore='" + lastLegionellaProtectionBefore + "'")
      .toString();
  }
}
