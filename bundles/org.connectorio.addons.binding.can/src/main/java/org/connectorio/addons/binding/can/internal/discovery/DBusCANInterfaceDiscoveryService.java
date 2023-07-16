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
package org.connectorio.addons.binding.can.internal.discovery;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import org.connectorio.addons.binding.can.CANInterface;
import org.connectorio.addons.binding.can.CANInterfaceTypes;
import org.connectorio.addons.binding.can.discovery.CANInterfaceDiscoveryDelegate;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnection.DBusBusType;
import org.freedesktop.dbus.interfaces.ObjectManager;
import org.freedesktop.dbus.types.Variant;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
@Component(service = DiscoveryService.class)
public class DBusCANInterfaceDiscoveryService extends AbstractDiscoveryService implements DiscoveryService {

  public static final String DEVICE_TYPE = "org.freedesktop.NetworkManager.Device";
  public static final String DEVICE_PATH = "/org/freedesktop/NetworkManager/Devices/";
  public static final String NETWORK_MANAGER_BUS = "org.freedesktop.NetworkManager";

  public static final String DRIVER_PROPERTY = "Driver";
  public static final String DRIVER_VCAN = "vcan";
  public static final String DRIVER_GS_USB = "gs_usb";
  public static final String INTERFACE_PROPERTY = "Interface";

  private final Logger logger = LoggerFactory.getLogger(DBusCANInterfaceDiscoveryService.class);
  private final List<CANInterfaceDiscoveryDelegate> participants = new CopyOnWriteArrayList<>();

  public DBusCANInterfaceDiscoveryService() {
    super(null, 30);
  }

  @Override
  protected void startBackgroundDiscovery() {
    scanInterfaces();
  }

  @Override
  protected void startScan() {
    scanInterfaces();
  }

  private void scanInterfaces() {
    try (DBusConnection connection = DBusConnection.getConnection(DBusBusType.SYSTEM, false, DBusConnection.TCP_CONNECT_TIMEOUT)) {
      ObjectManager nm = connection.getRemoteObject(NETWORK_MANAGER_BUS, "/org/freedesktop", ObjectManager.class);

      for (Entry<DBusPath, Map<String, Map<String, Variant<?>>>> pathEntry : nm.GetManagedObjects().entrySet()) {
        DBusPath key = pathEntry.getKey();

        if (key.getPath().startsWith(DEVICE_PATH)) {
          logger.debug("Inspecting dbus path: {}", key);

          Map<String, Map<String, Variant<?>>> value = pathEntry.getValue();
          for (Entry<String, Map<String, Variant<?>>> nodeEntry : value.entrySet()) {
            logger.trace("Inspection of device node: {}", nodeEntry);
            if (DEVICE_TYPE.equals(nodeEntry.getKey())) {
              logger.trace("Found device node, checking if its can interface: {}", nodeEntry.getKey());

              final Variant<?> driver = nodeEntry.getValue().get(DRIVER_PROPERTY);
              final Variant<?> name = nodeEntry.getValue().get(INTERFACE_PROPERTY);

              if (DRIVER_VCAN.equals(driver.getValue())) {
                notify(driver, name, "Virtual CAN interface"); //.ifPresent(this::thingDiscovered);
              } else if (DRIVER_GS_USB.equals(driver.getValue())) {
                notify(driver, name, "Socket CAN interface"); //.ifPresent(this::thingDiscovered);
              }
            }
          }
        }
      }

      connection.disconnect();
    } catch (Exception e) {
      logger.error("Discovery of can interfaces via dbus failed", e);
    }
  }

  private void notify(Variant<?> driver, Variant<?> name, String label) {
    if (name != null && CharSequence.class.equals(name.getType())) {
      final CANInterface canInterface = new CANInterface(((CharSequence) name.getValue()).toString(), CANInterfaceTypes.SOCKET_CAN);
      for (CANInterfaceDiscoveryDelegate participant : participants) {
        participant.interfaceAvailable(canInterface);
      }
    } else {
      logger.trace("Unidentifiable CAN interface {} which uses supported driver {}. Ignoring", name, driver);
    }
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void addDiscoveryDelegate(CANInterfaceDiscoveryDelegate participant) {
    participants.add(participant);
  }

  public void removeDiscoveryDelegate(CANInterfaceDiscoveryDelegate participant) {
    participants.remove(participant);
  }

}
