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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.can.CanNetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceType;
import org.connectorio.addons.network.iface.NetworkInterfaceUID;

public class DBusCanNetworkInterface implements CanNetworkInterface {

  private final String path;
  private final NetworkInterfaceType type;
  private final NetworkInterfaceUID id;
  private final String name;
  private final String displayName;
  private final String driver;
  private final List<Network> networks;
  private final AtomicBoolean up = new AtomicBoolean();

  public DBusCanNetworkInterface(String path, NetworkInterfaceType type, NetworkInterfaceUID id, String name, String displayName, String driver) {
    this(path, type, id, name, displayName, driver, new ArrayList<>());
  }

  public DBusCanNetworkInterface(String path, NetworkInterfaceType type, NetworkInterfaceUID id, String name, String displayName, String driver, List<Network> networks) {
    this.path = path;
    this.type = type;
    this.id = id;
    this.name = name;
    this.displayName = displayName;
    this.driver = driver;
    this.networks = networks;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String getDriver() {
    return driver;
  }

  @Override
  public boolean isUp() {
    return up.get();
  }

  @Override
  public NetworkInterfaceType getInterfaceType() {
    return type;
  }

  @Override
  public List<Network> getNetworks() {
    return networks;
  }

  @Override
  public NetworkInterfaceUID getUID() {
    return id;
  }

  @Override
  public String toString() {
    return "DBusCanNetworkInterface [" + id + "@'" + path + "', name=" + name + " " + getDisplayName() + " up=" + up + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DBusCanNetworkInterface)) {
      return false;
    }
    DBusCanNetworkInterface that = (DBusCanNetworkInterface) o;
    return Objects.equals(path, that.path) && Objects.equals(type, that.type)
        && Objects.equals(id, that.id) && Objects.equals(getName(), that.getName())
        && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getDriver(), that.getDriver());
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, type, id, getName(), getDisplayName(), getDriver());
  }

  String getPath() {
    return path;
  }

  boolean setUp(boolean up) {
    if (up) {
      // transition to UP state
      return this.up.compareAndSet(false, true);
    }

    // transition to DOWN state
    return this.up.compareAndSet(true, false);
  }

}
