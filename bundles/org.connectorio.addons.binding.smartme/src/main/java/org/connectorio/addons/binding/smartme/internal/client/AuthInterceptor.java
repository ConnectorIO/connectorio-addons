/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.smartme.internal.client;

import java.net.http.HttpRequest.Builder;
import java.util.Base64;
import java.util.function.Consumer;

public class AuthInterceptor implements Consumer<Builder> {

  private final String username;
  private final String password;

  public AuthInterceptor(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public void accept(Builder builder) {
    builder.header("Authorization",
      "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes())
    );
  }
}
