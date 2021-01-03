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

public class Session {

  private final String message;
  @JsonProperty("USER")
  private final String user;
  @JsonProperty("USER_ROLE")
  private final String role;
  @JsonProperty("STOKEN")
  private final String token;

  @JsonCreator
  public Session(@JsonProperty("message") String message, @JsonProperty("USER") String user, @JsonProperty("USER_ROLE") String role, @JsonProperty("STOKEN") String token) {
    this.message = message;
    this.user = user;
    this.role = role;
    this.token = token;
  }

  public String getMessage() {
    return message;
  }

  public String getUser() {
    return user;
  }

  public String getRole() {
    return role;
  }

  public String getToken() {
    return token;
  }

}
