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

public class TemperatureSettings {
  // "ACTIVE",
  @JsonProperty("USE_TEMP_SENSOR_0")
  @JsonDeserialize(using = ActiveBooleanDeserializer.class)
  private boolean useTemperatureSensor0;
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
  // "20"
  @JsonProperty("MINIMUM_TEMP")
  private String minimumTemperature;
  // "70"
  @JsonProperty("MAX_TEMP_SET_HEATER_STEP") // ??
  private String maximumTemperatureSetHeaterStep;
  // "70"
  @JsonProperty("MAX_TEMP_LOAD_SETPOINT_OR_FEEDIN")  // ??
  private String maximumTemperatureLoadSetpointOrFeedin;
  // "55"
  @JsonProperty("MAX_TEMP_LOW_TARIFF")
  private String maximumTemperatureLowTariff;
  // "55"
  @JsonProperty("MAX_TEMP_HEAT_PUMP_REQUEST")
  private String maximumTemperatureHeatPumpRequest;
  // "2"
  @JsonProperty("TEMPERATURE_HYSTERESIS")
  private String temperatureHysteresis;
  // "22:00"
  @JsonProperty("LOW_TARIFF_START_TIME")
  private String lowTariffStartTime;
  // "06:00"
  @JsonProperty("LOW_TARIFF_END_TIME")
  private String lowTariffEndTime;

  @Override
  public String toString() {
    return new StringJoiner(", ", TemperatureSettings.class.getSimpleName() + "[", "]")
      .add("useTemperatureSensor0=" + useTemperatureSensor0)
      .add("useTemperatureSensor1=" + useTemperatureSensor1)
      .add("useTemperatureSensor2=" + useTemperatureSensor2)
      .add("useTemperatureSensor3=" + useTemperatureSensor3)
      .add("useTemperatureSensor4=" + useTemperatureSensor4)
      .add("useTemperatureSensor5=" + useTemperatureSensor5)
      .add("minimumTemperature='" + minimumTemperature + "'")
      .add("maximumTemperatureSetHeaterStep='" + maximumTemperatureSetHeaterStep + "'")
      .add("maximumTemperatureLoadSetpointOrFeedin='" + maximumTemperatureLoadSetpointOrFeedin + "'")
      .add("maximumTemperatureLowTariff='" + maximumTemperatureLowTariff + "'")
      .add("maximumTemperatureHeatPumpRequest='" + maximumTemperatureHeatPumpRequest + "'")
      .add("temperatureHysteresis='" + temperatureHysteresis + "'")
      .add("lowTariffStartTime='" + lowTariffStartTime + "'")
      .add("lowTariffEndTime='" + lowTariffEndTime + "'")
      .toString();
  }

}
