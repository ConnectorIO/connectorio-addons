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

public class Errors {
  // "FALSE"
  @JsonProperty("TEMP_SENSOR_0")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean temperatureSensor0;
  // "FALSE"
  @JsonProperty("TEMP_SENSOR_1")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean temperatureSensor1;
  // "FALSE"
  @JsonProperty("TEMP_SENSOR_2")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean temperatureSensor2;
  // "FALSE"
  @JsonProperty("TEMP_SENSOR_3")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean temperatureSensor3;
  // "FALSE"
  @JsonProperty("TEMP_SENSOR_4")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean temperatureSensor4;
  // "FALSE"
  @JsonProperty("TEMP_SENSOR_5")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean temperatureSensor5;
  // "FALSE"
  @JsonProperty("RTC_INTERNET_SYNCHRONISATION")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean timeSynchronization;
  // "FALSE"
  @JsonProperty("TEMPERATURE_SETTINGS")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean temperatureSettings;
  // "FALSE"
  @JsonProperty("MISSING_CURRENT_FLOW")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean missingFlow;
  // "FALSE"
  @JsonProperty("LEGIONELLA_PROTECTION")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean legionellaProtection;
  // "FALSE"
  @JsonProperty("EXTENDED_COMMUNICATION")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean extendedCommunication;
  // "FALSE"
  @JsonProperty("WRONG_SETTINGS")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean wrongSettings;
  // "FALSE"
  @JsonProperty("RS485")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean rs485;
  // "FALSE"
  @JsonProperty("LAN")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean lan;
  // "FALSE"
  @JsonProperty("WIFI")
  @JsonDeserialize(using=TextBooleanDeserializer.class)
  private boolean wifi;

  public Errors() {
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Errors.class.getSimpleName() + "[", "]")
      .add("temperatureSensor0=" + temperatureSensor0)
      .add("temperatureSensor1=" + temperatureSensor1)
      .add("temperatureSensor2=" + temperatureSensor2)
      .add("temperatureSensor3=" + temperatureSensor3)
      .add("temperatureSensor4=" + temperatureSensor4)
      .add("temperatureSensor5=" + temperatureSensor5)
      .add("timeSynchronization=" + timeSynchronization)
      .add("temperatureSettings=" + temperatureSettings)
      .add("missingFlow=" + missingFlow)
      .add("legionellaProtection=" + legionellaProtection)
      .add("extendedCommunication=" + extendedCommunication)
      .add("wrongSettings=" + wrongSettings)
      .add("rs485=" + rs485)
      .add("lan=" + lan)
      .add("wifi=" + wifi)
      .toString();
  }
}
