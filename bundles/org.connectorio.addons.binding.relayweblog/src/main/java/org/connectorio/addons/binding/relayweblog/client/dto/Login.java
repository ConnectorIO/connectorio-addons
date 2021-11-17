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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.RandomStringUtils;

@JsonPropertyOrder({"payload", "password"})
public class Login {

  private final String payload;
  private final String password;

  @JsonCreator
  public Login(@JsonProperty("password") String password, @JsonProperty("payload") String payload) {
    this.payload = payload;
    this.password = password;
  }

  public Login(@JsonProperty("password") String password) {
    this(password, RandomStringUtils.randomAlphanumeric(16));
  }

  public String getPayload() {
    return payload;
  }

  public String getPassword() {
    return password;
  }

}
