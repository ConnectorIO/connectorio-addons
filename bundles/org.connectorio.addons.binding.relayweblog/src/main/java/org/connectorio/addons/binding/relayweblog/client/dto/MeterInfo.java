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
package org.connectorio.addons.binding.relayweblog.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeterInfo {

  @JsonProperty("UID")
  private String id; // "1"
  @JsonProperty("PA")
  private String PA; // "0"
  @JsonProperty("ID")
  private String identifier; // "01400001"
  @JsonProperty("MAN")
  private String manufacturer; // "SON"
  @JsonProperty("VER")
  private String version; // "4"
  @JsonProperty("MEDIUM")
  private String medium; // "Wärme"

  @JsonProperty("BAUD")
  private String baud; // "2400"

  @JsonProperty("NKE")
  private String NKE; // "No"
  @JsonProperty("APPRES")
  private String APPRES; // "No"

  @JsonProperty("INT")
  private String INT; // "0"
  @JsonProperty("INT_NAME")
  private String INT_NAME; // ""
  @JsonProperty("GROUP")
  private String GROUP; // ""
  @JsonProperty("TXT1")
  private String TXT1; // ""
  @JsonProperty("TXT2")
  private String TXT2; // ""

  public String getId() {
    return id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public String getVersion() {
    return version;
  }

  public String getMedium() {
    return medium;
  }

}
