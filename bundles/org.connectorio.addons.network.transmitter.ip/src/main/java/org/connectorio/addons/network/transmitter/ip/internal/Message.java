/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.network.transmitter.ip.internal;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class Message {

  private final CompletableFuture<Void> callback;

  private final InetSocketAddress address;
  private final byte[] payload;

  public Message(CompletableFuture<Void> callback, InetSocketAddress address, byte[] payload) {
    this.callback = callback;
    this.address = address;
    this.payload = payload;
  }

  public CompletableFuture<Void> getCallback() {
    return callback;
  }

  public InetSocketAddress getAddress() {
    return address;
  }

  public byte[] getPayload() {
    return payload;
  }

}
