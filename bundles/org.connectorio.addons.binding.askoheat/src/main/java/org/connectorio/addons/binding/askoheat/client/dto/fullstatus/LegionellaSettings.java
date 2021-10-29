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

public class LegionellaSettings {
  // "NOT ACTIVE"
  @JsonProperty("PROTECTION")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean protection;
  // "NOT ACTIVE"
  @JsonProperty("PERIOD_DAILY")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean periodDaily;
  // "NOT ACTIVE"
  @JsonProperty("PERIOD_WEEKLY")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean periodWeekly;
  // "ACTIVE"
  @JsonProperty("PERIOD_FORTNIGHTLY") // bi-weekly
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean periodFortnightly;
  // "NOT ACTIVE"
  @JsonProperty("PERIOD_MONTHLY")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean period_monthly;
  // "ACTIVE"
  @JsonProperty("USE_TEMP_SENSOR_0")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useTemperatureSensor;
  // "NOT ACTIVE"
  @JsonProperty("USE_TEMP_SENSOR_1")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useTemperatureSensor1;
  // "NOT ACTIVE"
  @JsonProperty("USE_TEMP_SENSOR_2")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useTemperatureSensor2;
  // "NOT ACTIVE"
  @JsonProperty("USE_TEMP_SENSOR_3")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useTemperatureSensor3;
  // "NOT ACTIVE"
  @JsonProperty("USE_TEMP_SENSOR_4")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useTemperatureSensor4;
  // "NOT ACTIVE"
  @JsonProperty("USE_TEMP_SENSOR_5")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useTemperatureSensor5;
  // "65 °C"
  @JsonProperty("PROTECTION_TEMPERATURE")
  private String protectionTemperature;
  // "00:00"
  @JsonProperty("PREFERED_ACTIVATION_TIME")
  private String preferredActivationTime;
  // "240 min.
  @JsonProperty("MAXIMUM_HEATUP_TIME")
  private String maximumHeatupTime;

  @Override
  public String toString() {
    return new StringJoiner(", ", LegionellaSettings.class.getSimpleName() + "[", "]")
      .add("protection=" + protection)
      .add("periodDaily=" + periodDaily)
      .add("periodWeekly=" + periodWeekly)
      .add("periodFortnightly=" + periodFortnightly)
      .add("period_monthly=" + period_monthly)
      .add("useTemperatureSensor=" + useTemperatureSensor)
      .add("useTemperatureSensor1=" + useTemperatureSensor1)
      .add("useTemperatureSensor2=" + useTemperatureSensor2)
      .add("useTemperatureSensor3=" + useTemperatureSensor3)
      .add("useTemperatureSensor4=" + useTemperatureSensor4)
      .add("useTemperatureSensor5=" + useTemperatureSensor5)
      .add("protectionTemperature='" + protectionTemperature + "'")
      .add("preferredActivationTime='" + preferredActivationTime + "'")
      .add("maximumHeatupTime='" + maximumHeatupTime + "'")
      .toString();
  }
}
