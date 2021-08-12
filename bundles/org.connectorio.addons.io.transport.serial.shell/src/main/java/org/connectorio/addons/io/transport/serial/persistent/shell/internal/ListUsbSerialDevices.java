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
package org.connectorio.addons.io.transport.serial.persistent.shell.internal;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openhab.core.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.openhab.core.config.discovery.usbserial.UsbSerialDiscovery;
import org.openhab.core.config.discovery.usbserial.UsbSerialDiscoveryListener;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which lists discoverable usb serial devices.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class ListUsbSerialDevices extends AbstractConsoleCommandExtension {

  private final UsbSerialDiscovery usbSerialDiscovery;

  @Activate
  public ListUsbSerialDevices(@Reference UsbSerialDiscovery usbSerialDiscovery) {
    super("co7io-list-usb-ports", "List usb ports detected by system.");
    this.usbSerialDiscovery = usbSerialDiscovery;
  }

  @Override
  public void execute(String[] args, Console console) {
    console.println("Detected ports:");
    Set<UsbSerialDeviceInformation> devices = new LinkedHashSet<>();
    UsbSerialDiscoveryListener listener = new UsbSerialDiscoveryListener() {
      @Override
      public void usbSerialDeviceDiscovered(UsbSerialDeviceInformation deviceInformation) {
        devices.add(deviceInformation);
      }
      @Override
      public void usbSerialDeviceRemoved(UsbSerialDeviceInformation deviceInformation) {
        devices.remove(deviceInformation);
      }
    };

    try {
      usbSerialDiscovery.registerDiscoveryListener(listener);
      usbSerialDiscovery.doSingleScan();
    } finally {
      usbSerialDiscovery.unregisterDiscoveryListener(listener);
    }

    int index = 1;
    for (UsbSerialDeviceInformation deviceInformation : devices) {
      console.println(index++ + ") " + deviceInformation.getSerialPort());
      console.println("\t               Product: " + deviceInformation.getProduct());
      console.println("\t Interface Description: " + deviceInformation.getInterfaceDescription());
      console.println("\t      Interface Number: " + deviceInformation.getInterfaceNumber());
      console.println("\t            Product ID: " + deviceInformation.getProductId());
      console.println("\t             Vendor ID: " + deviceInformation.getVendorId());
      console.println("\t          Manufacturer: " + deviceInformation.getManufacturer());
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-list-serial-ports");
  }

}
