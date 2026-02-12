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
package org.connectorio.addons.network.can.dbus.internal;

import java.util.Objects;
import org.connectorio.addons.network.NetworkType;
import org.connectorio.addons.network.NetworkUID;
import org.connectorio.addons.network.can.CanNetwork;

public class DBusCanNetwork implements CanNetwork {

  private final NetworkType type;
  private final NetworkUID id;

  public DBusCanNetwork(NetworkType networkType, NetworkUID id) {
    this.type = networkType;
    this.id = id;
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
    if (!(o instanceof DBusCanNetwork)) {
      return false;
    }
    DBusCanNetwork that = (DBusCanNetwork) o;
    return Objects.equals(getType(), that.getType()) && Objects.equals(id,
        that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), id);
  }

}
