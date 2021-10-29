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
import java.util.StringJoiner;

public class DeviceInfo {
  // Fri, 2021-10-22 22:21:13",
  @JsonProperty("DATETIME")
  private String datetime;
  // 012-6793",
  @JsonProperty("ARTICLE_NUMBER")
  private String articleNumber;
  // AHFR-BI-plus-4.4",
  @JsonProperty("ARTICLE_NAME")
  private String articleName;
  // 2216716.0005",
  @JsonProperty("SERIAL_NUMBER")
  private String serialNumber;
  // 650 watt",
  @JsonProperty("HEATER_1_POWER")
  private String heater1power;
  // 1250 watt",
  @JsonProperty("HEATER_2_POWER")
  private String heater2power;
  // 2500 watt",
  @JsonProperty("HEATER_3_POWER")
  private String heater3power;
  // FLANGE DELTA CONNECTION 7 STAGES",
  @JsonProperty("TYPE")
  private String type;
  // UNKNOWN",
  @JsonProperty("HEATER_POSITION")
  private String heaterPosition;
  // HW 1.3 -> app0",
  @JsonProperty("HARDWARE_VERSION")
  private String hardwareVersion;
  // 4.3.1",
  @JsonProperty("SOFTWARE_VERSION")
  private String softwareVersion;
  // NEW 2216716.0005",
  @JsonProperty("INFOSTRING")
  private String infos;
  // 192.168.43.114"
  @JsonProperty("LOCAL_IP_ADDRESS")
  private String ip;

  public String getDatetime() {
    return datetime;
  }

  public String getArticleNumber() {
    return articleNumber;
  }

  public String getArticleName() {
    return articleName;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public String getHeater1power() {
    return heater1power;
  }

  public String getHeater2power() {
    return heater2power;
  }

  public String getHeater3power() {
    return heater3power;
  }

  public String getType() {
    return type;
  }

  public String getHeaterPosition() {
    return heaterPosition;
  }

  public String getHardwareVersion() {
    return hardwareVersion;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public String getInfos() {
    return infos;
  }

  public String getIp() {
    return ip;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DeviceInfo.class.getSimpleName() + "[", "]")
      .add("datetime='" + datetime + "'")
      .add("articleNumber='" + articleNumber + "'")
      .add("articleName='" + articleName + "'")
      .add("serialNumber='" + serialNumber + "'")
      .add("heater1power='" + heater1power + "'")
      .add("heater2power='" + heater2power + "'")
      .add("heater3power='" + heater3power + "'")
      .add("type='" + type + "'")
      .add("heaterPosition='" + heaterPosition + "'")
      .add("hardwareVersion='" + hardwareVersion + "'")
      .add("softwareVersion='" + softwareVersion + "'")
      .add("infos='" + infos + "'")
      .add("ip='" + ip + "'")
      .toString();
  }
}
