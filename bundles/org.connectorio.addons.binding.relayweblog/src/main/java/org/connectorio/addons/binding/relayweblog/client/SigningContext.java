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

package org.connectorio.addons.binding.relayweblog.client;

import static org.connectorio.addons.binding.relayweblog.client.Hash.hmacsha512;
import static org.connectorio.addons.binding.relayweblog.client.Hash.sha512;

public class SigningContext {

  private final String passwordHash;
  private String session;

  public SigningContext(String passwordHash) {
    this.passwordHash = sha512(passwordHash);
  }

  public void setSession(String sessionToken) {
    if (this.session != null) {
      throw new IllegalArgumentException("Session already set");
    }
    this.session = sessionToken;
  }

  public void resetSession() {
    this.session = null;
  }

  public String getSession() {
    return session;
  }

  public String getHash(String uri, long microtime) {
    return hmacsha512(uri + microtime, getHmacKey());
  }

  public String getHash(String uri, long microtime, String payload) {
    return hmacsha512(uri + microtime + payload, getHmacKey());
  }

  public String getHmacKey() {
    if (session == null) {
      return passwordHash;
    }
    return sha512(passwordHash + session);
  }

}