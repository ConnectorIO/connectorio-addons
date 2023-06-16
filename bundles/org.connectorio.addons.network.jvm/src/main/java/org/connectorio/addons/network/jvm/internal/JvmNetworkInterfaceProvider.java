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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.network.ip.IpNetworkInterfaceTypes;
import org.connectorio.addons.network.ip.IpNetworkTypes;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.NetworkUID;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceProvider;
import org.connectorio.addons.network.iface.NetworkInterfaceStateCallback;
import org.connectorio.addons.network.iface.NetworkInterfaceType;
import org.connectorio.addons.network.iface.NetworkInterfaceUID;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = NetworkInterfaceProvider.class)
public class JvmNetworkInterfaceProvider implements NetworkInterfaceProvider, Runnable {

  private final Logger logger = LoggerFactory.getLogger(JvmNetworkInterfaceProvider.class);
  private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor((runnable) -> {
    Thread thread = new Thread(runnable, "jvm-nif-monitor");
    thread.setUncaughtExceptionHandler((t, e) -> {
      logger.error("Error while running network related thread", e);
    });
    return thread;
  });

  private final Set<ProviderChangeListener<NetworkInterface>> listeners = new CopyOnWriteArraySet<>();
  private final Set<NetworkInterfaceStateCallback> callbacks = new CopyOnWriteArraySet<>();
  private final Set<JvmNetworkInterface> networkInterfaces = new CopyOnWriteArraySet<>();

  @Activate
  public JvmNetworkInterfaceProvider() {
    monitor.scheduleAtFixedRate(this::scan, 0, 5, TimeUnit.SECONDS);
    monitor.scheduleAtFixedRate(this, 3, 5, TimeUnit.SECONDS);
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
  }

  private void scan() {
    try {
      Enumeration<java.net.NetworkInterface> en = java.net.NetworkInterface.getNetworkInterfaces();
      while (en.hasMoreElements()) {
        java.net.NetworkInterface networkInterface = en.nextElement();

        NetworkInterfaceType type = networkInterface.isLoopback() ? IpNetworkInterfaceTypes.LOOPBACK : IpNetworkInterfaceTypes.IP;

        List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
        List<Network> networks = new ArrayList<>();
        for (InterfaceAddress address : addresses) {
          InetAddress inetAddress = address.getAddress();
          InetAddress broadcast = address.getBroadcast();

          if (inetAddress instanceof Inet4Address) {
            networks.add(new JvmNetwork(
              IpNetworkTypes.IPv4, new NetworkUID(IpNetworkTypes.IPv4.getType(), zerofill(inetAddress)),
              inetAddress.getHostAddress(), broadcast == null ? "" : broadcast.getHostAddress()
            ));
          }
        }

        add(new JvmNetworkInterface(type, new NetworkInterfaceUID(type.getType(), networkInterface.getName()),
          mac(networkInterface.getHardwareAddress(), ":"), networkInterface.getName(), networkInterface.getDisplayName(), networks
        ));
      }

    } catch (SocketException e) {
      logger.warn("Could not fetch network interfaces", e);
    } catch (Exception e) {
      logger.error("Error while retrieving network interface information", e);
    }
  }

  private String zerofill(InetAddress inetAddress) {
    byte[] address = inetAddress.getAddress();
    return String.format("%03d-%03d-%03d-%03d", address[0] & 0xff, address[1] & 0xff, address[2] & 0xff, address[3] & 0xff);
  }

  private void add(JvmNetworkInterface networkInterface) {
    if (networkInterfaces.add(networkInterface)) {
      listeners.forEach(listener -> listener.added(this, networkInterface));
    }
  }

  private void remove(JvmNetworkInterface networkInterface) {
    if (networkInterfaces.remove(networkInterface)) {
      listeners.forEach(listener -> listener.removed(this, networkInterface));
    }
  }

  private void changed(JvmNetworkInterface networkInterface) {
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
  public void run() {
    for (JvmNetworkInterface networkInterface : networkInterfaces) {
      try {
        java.net.NetworkInterface iface = java.net.NetworkInterface.getByName(networkInterface.getName());
        if (iface == null) {
          // make sure we emmit "down" notification first, if interface was last known as "up"
          if (networkInterface.setUp(false)) {
            statusChange(networkInterface, false);
          }
          remove(networkInterface);
          continue;
        }

        boolean up = iface.isUp();
        if (networkInterface.setUp(up)) {
          statusChange(networkInterface, up);
        }
      } catch (SocketException e) {
        remove(networkInterface);
      }
    }
  }

  private void statusChange(JvmNetworkInterface networkInterface, boolean up) {
    for (NetworkInterfaceStateCallback callback : callbacks) {
      if (up) {
        callback.networkInterfaceUp(networkInterface);
        continue;
      }
      callback.networkInterfaceDown(networkInterface);
    }
  }

  private static String mac(byte[] hardwareAddress, String separator) {
    if (hardwareAddress == null) {
      return "";
    }

    String hexadecimal = "";
    for (int i = 0; i < hardwareAddress.length; i++) {
      hexadecimal +=  (hexadecimal.length() > 0 ? separator : "") + String.format("%02X", hardwareAddress[i]);
    }
    return hexadecimal;
  }

}
