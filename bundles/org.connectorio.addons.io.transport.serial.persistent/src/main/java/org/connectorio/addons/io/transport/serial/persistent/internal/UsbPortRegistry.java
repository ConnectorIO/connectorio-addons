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
package org.connectorio.addons.io.transport.serial.persistent.internal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.connectorio.addons.io.transport.serial.persistent.UsbRegistry;
import org.openhab.core.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.openhab.core.config.discovery.usbserial.UsbSerialDiscovery;
import org.openhab.core.config.discovery.usbserial.UsbSerialDiscoveryListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = UsbRegistry.class)
public class UsbPortRegistry implements UsbSerialDiscoveryListener, UsbRegistry {

  private final UsbSerialDiscovery usbSerialDiscovery;
  private final Set<UsbSerialDeviceInformation> ports = Collections.synchronizedSet(new LinkedHashSet<>());

  @Activate
  public UsbPortRegistry(@Reference UsbSerialDiscovery discovery) {
    this.usbSerialDiscovery = discovery;
    discovery.registerDiscoveryListener(this);
  }

  public Set<UsbSerialDeviceInformation> getPorts() {
    if (ports.isEmpty()) {
      usbSerialDiscovery.doSingleScan();
    }
    return Collections.unmodifiableSet(ports);
  }

  @Deactivate
  void deactivate() {
    usbSerialDiscovery.unregisterDiscoveryListener(this);
  }

  @Override
  public void usbSerialDeviceDiscovered(UsbSerialDeviceInformation deviceInformation) {
    add(deviceInformation);
  }

  protected final void add(UsbSerialDeviceInformation deviceInformation) {
    ports.add(deviceInformation);
  }

  @Override
  public void usbSerialDeviceRemoved(UsbSerialDeviceInformation deviceInformation) {
    ports.remove(deviceInformation);
  }

}
