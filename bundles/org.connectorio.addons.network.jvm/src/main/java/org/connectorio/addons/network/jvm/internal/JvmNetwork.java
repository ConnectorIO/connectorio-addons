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
package org.connectorio.addons.network.jvm.internal;

import java.util.Objects;
import org.connectorio.addons.network.ip.IpNetwork;
import org.connectorio.addons.network.NetworkType;
import org.connectorio.addons.network.NetworkUID;

public class JvmNetwork implements IpNetwork {

  private final NetworkType type;

  private final NetworkUID id;

  private final String address;

  private final String broadcastAddress;

  public JvmNetwork(NetworkType type, NetworkUID id, String address, String broadcastAddress) {
    this.type = type;
    this.id = id;
    this.broadcastAddress = broadcastAddress;
    this.address = address;
  }

  @Override
  public String getBroadcastAddress() {
    return broadcastAddress;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public NetworkType getType() {
    return type;
  }

  @Override
  public NetworkUID getUID() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JvmNetwork)) {
      return false;
    }
    JvmNetwork that = (JvmNetwork) o;
    return Objects.equals(getType(), that.getType()) && Objects.equals(id, that.id)
        && Objects.equals(getAddress(), that.getAddress())
        && Objects.equals(getBroadcastAddress(), that.getBroadcastAddress());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), id, getAddress(), getBroadcastAddress());
  }

  @Override
  public String toString() {
    return "JvmNetwork [" + id + " " + address + " " + broadcastAddress + "]";
  }
}
