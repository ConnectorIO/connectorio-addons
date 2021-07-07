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

import java.net.URI;
import java.util.stream.Stream;
import org.connectorio.addons.io.transport.serial.persistent.SerialPortDelegate;
import org.connectorio.addons.io.transport.serial.persistent.UsbRegistry;
import org.openhab.core.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.openhab.core.io.transport.serial.ProtocolType;
import org.openhab.core.io.transport.serial.ProtocolType.PathType;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = SerialPortProvider.class)
public class PersistentSerialPortProvider implements SerialPortProvider {

  private final Logger logger = LoggerFactory.getLogger(PersistentSerialPortProvider.class);
  private final SerialPortDelegate delegate;
  private final UsbRegistry usbPortRegistry;

  @Activate
  public PersistentSerialPortProvider(@Reference SerialPortDelegate delegate, @Reference UsbRegistry usbPortRegistry) {
    this.delegate = delegate;
    this.usbPortRegistry = usbPortRegistry;
  }

  @Override
  public SerialPortIdentifier getPortIdentifier(URI portName) {
    if (PersistentPortIdentifier.isPersistent(portName)) {
      PersistentPortIdentifier portIdentifier = PersistentPortIdentifier.fromUri(delegate, portName);
      for (UsbSerialDeviceInformation deviceInformation : usbPortRegistry.getPorts()) {
        if (portIdentifier.getUsbIdentifier().equals(new UsbIdentifier(deviceInformation))) {
          return createPersistentPortIdentifier(deviceInformation);
        }
      }
    }

    return null;
  }

  @Override
  public Stream<ProtocolType> getAcceptedProtocols() {
    return Stream.of(new ProtocolType(PathType.LOCAL, "persistent"));
  }

  @Override
  public Stream<SerialPortIdentifier> getSerialPortIdentifiers() {
    return usbPortRegistry.getPorts().stream()
      .map(this::createPersistentPortIdentifier);
  }

  private PersistentPortIdentifier createPersistentPortIdentifier(UsbSerialDeviceInformation deviceInformation) {
    return new PersistentPortIdentifier(delegate, new UsbIdentifier(deviceInformation),
      deviceInformation.getProductId(),
      deviceInformation.getVendorId(),
      deviceInformation.getProduct(),
      deviceInformation.getManufacturer(),
      deviceInformation.getInterfaceDescription(),
      deviceInformation.getInterfaceNumber(),
      deviceInformation.getSerialNumber(),
      deviceInformation.getSerialPort()
    );
  }

}
