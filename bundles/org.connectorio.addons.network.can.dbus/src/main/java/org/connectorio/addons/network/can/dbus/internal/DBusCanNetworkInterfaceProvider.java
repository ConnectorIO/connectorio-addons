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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.connectorio.addons.network.NetworkUID;
import org.connectorio.addons.network.can.CanNetworkInterfaceTypes;
import org.connectorio.addons.network.can.CanNetworkTypes;
import org.connectorio.addons.network.can.dbus.internal.nm.InterfaceFlags;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceProvider;
import org.connectorio.addons.network.iface.NetworkInterfaceStateCallback;
import org.connectorio.addons.network.iface.NetworkInterfaceType;
import org.connectorio.addons.network.iface.NetworkInterfaceUID;
import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnection.DBusBusType;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.interfaces.ObjectManager;
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.types.Variant;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = NetworkInterfaceProvider.class, immediate = true)
public class DBusCanNetworkInterfaceProvider implements NetworkInterfaceProvider, DBusSigHandler<PropertiesChanged> {

  public static final String PROPERTIES_TYPE = "org.freedesktop.DBus.Properties";
  public static final String DEVICE_TYPE = "org.freedesktop.NetworkManager.Device";
  public static final String DEVICE_GENERIC_TYPE = "org.freedesktop.NetworkManager.Device.Generic";
  public static final String DEVICE_PATH = "/org/freedesktop/NetworkManager/Devices/";
  public static final String NETWORK_MANAGER_BUS = "org.freedesktop.NetworkManager";

  public static final String DRIVER_PROPERTY = "Driver";
  public static final String TYPE_DESCRIPTION_VCAN = "vcan";
  public static final String TYPE_DESCRIPTION_CAN = "can";
  public static final String INTERFACE_PROPERTY = "Interface";
  public static final String TYPE_DESCRIPTION_PROPERTY = "TypeDescription";

  private final Logger logger = LoggerFactory.getLogger(DBusCanNetworkInterfaceProvider.class);

  private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor((runnable) -> {
    Thread thread = new Thread(runnable, "can-interface-monitor");
    thread.setUncaughtExceptionHandler((t, e) -> {
      logger.error("Error while running can network related thread", e);
    });
    return thread;
  });

  private final Map<DBusCanNetworkInterface, DBusSigHandler<PropertiesChanged>> subscriptions = new ConcurrentHashMap<>();

  private final Set<ProviderChangeListener<NetworkInterface>> listeners = new CopyOnWriteArraySet<>();
  private final Set<NetworkInterfaceStateCallback> callbacks = new CopyOnWriteArraySet<>();
  private final Set<DBusCanNetworkInterface> networkInterfaces = new CopyOnWriteArraySet<>();

  private DBusConnection connection;
  private String owner;

  @Activate
  public DBusCanNetworkInterfaceProvider() {
    try {
      withConnection((cn) -> {
        // enable scanning only if we acquire dbus connection
        monitor.scheduleAtFixedRate(this::scan, 0, 30, TimeUnit.SECONDS);

        try {
          DBus dbus = cn.getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus", DBus.class);
          owner = dbus.GetNameOwner(NETWORK_MANAGER_BUS);
          connection.addSigHandler(PropertiesChanged.class, owner, this);
        } catch (DBusException e) {
          logger.error("Failed to setup NetworkManager's interface state tracker", e);
        }
      });
    } catch (DBusException e) {
      logger.error("Failed to obtain DBus connection, DBusCanNetworkInterfaceProvider is unable to scan available CAN interfaces.", e);
    }
  }

  @Override
  public void addNetworkInterfaceStateCallback(NetworkInterfaceStateCallback networkInterfaceStateCallback) {
    callbacks.add(networkInterfaceStateCallback);
  }

  @Override
  public void removeNetworkInterfaceStateCallback(NetworkInterfaceStateCallback networkInterfaceStateCallback) {
    callbacks.remove(networkInterfaceStateCallback);
  }

  @Deactivate
  public void destroy() {
    monitor.shutdownNow();

    if (connection != null) {
      if (owner != null) {
        try {
          connection.removeSigHandler(PropertiesChanged.class, owner, this);
        } catch (DBusException e) {
          logger.warn("Failed to remove NetworkManager's signal handler", e);
        }
      }
      try {
        connection.close();
      } catch (IOException e) {
        logger.warn("Error while closing DBus connection", e);
      }
    }
  }

  private void scan() {
    try {
      ObjectManager nm = connection.getRemoteObject(NETWORK_MANAGER_BUS, "/org/freedesktop", ObjectManager.class);

      for (Entry<DBusPath, Map<String, Map<String, Variant<?>>>> pathEntry : nm.GetManagedObjects().entrySet()) {
        DBusPath key = pathEntry.getKey();

        if (key.getPath().startsWith(DEVICE_PATH)) {
          logger.debug("Inspecting dbus path: {}", key);

          Map<String, Map<String, Variant<?>>> value = pathEntry.getValue();
          for (Entry<String, Map<String, Variant<?>>> nodeEntry : value.entrySet()) {
            logger.trace("Inspection of device node: {}", nodeEntry);
            if (DEVICE_GENERIC_TYPE.equals(nodeEntry.getKey())) {
              logger.trace("Found generic device node, checking if its can interface: {} {}", nodeEntry.getKey(), nodeEntry.getValue());

              final Variant<?> typeDescription = nodeEntry.getValue().get(TYPE_DESCRIPTION_PROPERTY);
              Map<String, Variant<?>> deviceInfo = value.get(DEVICE_TYPE);
              if (typeDescription == null || deviceInfo == null) {
                continue;
              }

              final Variant<?> driver = deviceInfo.get(DRIVER_PROPERTY);
              final Variant<?> name = deviceInfo.get(INTERFACE_PROPERTY);
              InterfaceFlags interfaceFlags = InterfaceFlags.from(deviceInfo);

              if (name != null && TYPE_DESCRIPTION_VCAN.equals(typeDescription.getValue())) {
                // here, based on driver we can be certain that this interface is CAN
                notify(key.getPath(), interfaceFlags.isUp(), CanNetworkInterfaceTypes.VCAN, String.valueOf(driver.getValue()), name, "Virtual CAN interface " + name);
              } else if (TYPE_DESCRIPTION_CAN.equals(typeDescription.getValue())) {
                notify(key.getPath(), interfaceFlags.isUp(), CanNetworkInterfaceTypes.SOCKETCAN, String.valueOf(driver.getValue()), name, "SocketCAN interface " + name + " backed by " + driver + " driver");
              }
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error("Discovery of can interfaces via dbus failed", e);
    }
  }

  private void withConnection(Consumer<DBusConnection> consumer) throws DBusException {
    if (connection == null) {
      connection = DBusConnection.getConnection(DBusBusType.SYSTEM, false, DBusConnection.TCP_CONNECT_TIMEOUT);
    }
    consumer.accept(connection);
  }

  private void notify(String busPath, boolean up, NetworkInterfaceType interfaceType, String driver, Variant<?> name, String label) {
    if (name != null && CharSequence.class.equals(name.getType())) {
      String interfaceName = name.getValue().toString();
      DBusCanNetworkInterface canNetworkInterface = new DBusCanNetworkInterface(
        busPath,
        interfaceType,
        new NetworkInterfaceUID("can", driver, interfaceName),
        interfaceName, label, driver, Arrays.asList(
          new DBusCanNetwork(CanNetworkTypes.CANOPEN, new NetworkUID("can", driver, interfaceName, "canopen")),
          new DBusCanNetwork(CanNetworkTypes.CAN_RAW, new NetworkUID("can", driver, interfaceName, "raw"))
        )
      );
      canNetworkInterface.setUp(up);
      add(canNetworkInterface);
    } else {
      logger.trace("Unidentifiable CAN interface {} which uses supported driver {}. Ignoring", name, driver);
    }
  }

  private void add(DBusCanNetworkInterface networkInterface) {
    if (networkInterfaces.add(networkInterface)) {
      listeners.forEach(listener -> listener.added(this, networkInterface));
    }
  }

  private void remove(DBusCanNetworkInterface networkInterface) {
    if (networkInterfaces.remove(networkInterface)) {
      listeners.forEach(listener -> listener.removed(this, networkInterface));
    }
    DBusSigHandler<PropertiesChanged> handler = subscriptions.get(networkInterface);
    if (handler != null) {
      try {
        connection.removeSigHandler(PropertiesChanged.class, networkInterface.getPath(), handler);
      } catch (DBusException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void changed(DBusCanNetworkInterface networkInterface) {
    listeners.forEach(listener -> listener.updated(this, networkInterface, networkInterface));
  }

  @Override
  public Collection<NetworkInterface> getAll() {
    return Collections.unmodifiableSet(networkInterfaces);
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<NetworkInterface> listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<NetworkInterface> listener) {
    listeners.remove(listener);
  }

  @Override
  public void handle(PropertiesChanged propertiesChanged) {
    String path = propertiesChanged.getPath();
    Map<String, Variant<?>> properties = propertiesChanged.getPropertiesChanged();
    InterfaceFlags interfaceFlags = InterfaceFlags.from(properties);
    if (interfaceFlags == null) {
      return;
    }

    for (DBusCanNetworkInterface canNetworkInterface : networkInterfaces) {
      if (path.equals(canNetworkInterface.getPath())) {
        logger.info("Received state change notification for interface {}, flags={}", canNetworkInterface.getName(), interfaceFlags);
        statusChange(canNetworkInterface, canNetworkInterface.setUp(interfaceFlags.isUp()));
        return;
      }
    }

    logger.debug("Received state change notification for unknown interface {}, flags={}", path, interfaceFlags);
  }

  private void statusChange(DBusCanNetworkInterface networkInterface, boolean up) {
    for (NetworkInterfaceStateCallback callback : callbacks) {
      if (up) {
        callback.networkInterfaceUp(networkInterface);
        continue;
      }
      callback.networkInterfaceDown(networkInterface);
    }
  }

}
