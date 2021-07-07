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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Condition;
import org.assertj.core.data.Index;
import org.connectorio.addons.io.transport.serial.persistent.SerialPortDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.openhab.core.config.discovery.usbserial.UsbSerialDiscovery;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;

@ExtendWith(MockitoExtension.class)
class PersistentSerialPortProviderTest {

  public static final String TTY_USB_0 = "/dev/ttyUSB0";
  public static final String TTY_USB_2 = "/dev/ttyUSB2";
  public static final String TTY_ACM_0 = "/dev/ttyACM0";

  @Mock
  UsbSerialDiscovery discovery;

  @Mock
  SerialPortManager portManager;

  @Mock
  SerialPortDelegate delegate;

  UsbPortRegistry registry;

  @BeforeEach
  void initialize() {
    registry = new UsbPortRegistry(discovery) {
      {{
        add(createDevice(0x20, 0x10, TTY_USB_0));
        add(createDevice(0x20, 0x11, TTY_USB_2));
        add(createDevice(0x20, 0x12, TTY_ACM_0));
      }}
    };
  }

  @Test
  void testSerialPortIdentifiers() {
    PersistentSerialPortProvider provider = new PersistentSerialPortProvider(delegate, registry);

    List<String> identifiers = provider.getSerialPortIdentifiers().map(SerialPortIdentifier::getName).collect(Collectors.toList());

    assertThat(identifiers).hasSize(3)
      .has(portNameCondition(TTY_USB_0), Index.atIndex(0))
      .has(portNameCondition(TTY_USB_2), Index.atIndex(1))
      .has(portNameCondition(TTY_ACM_0), Index.atIndex(2));
  }

  @Test
  void testSerialPortOpen() throws Exception {
    PersistentSerialPortProvider provider = new PersistentSerialPortProvider(delegate, registry);

    SerialPortIdentifier identifier = provider.getSerialPortIdentifiers().collect(Collectors.toList()).get(0);
    assertThat(identifier).isNotNull();

    SerialPortIdentifier delegatePort = mock(SerialPortIdentifier.class, TTY_USB_0 + " identifier");
    SerialPort serialPort = mock(SerialPort.class, TTY_USB_0 + " port");
    when(delegatePort.open("test", 10)).thenReturn(serialPort);

    when(delegate.lookup((PersistentPortIdentifier) identifier)).thenReturn(delegatePort);
    SerialPort port = identifier.open("test", 10);

    assertThat(port).isNotNull()
      .isEqualTo(serialPort);

    verify(delegate).lookup(argThat(new ArgumentMatcher<PersistentPortIdentifier>() {
      @Override
      public boolean matches(PersistentPortIdentifier argument) {
        return argument.getName().endsWith(TTY_USB_0);
      }
    }));
  }

  private Condition<String> portNameCondition(String portName) {
    return new Condition<>(name -> name.endsWith(portName), "Port is " + portName);
  }

  private UsbSerialDeviceInformation createDevice(int vendorId, int productId, String port) {
    return new UsbSerialDeviceInformation(vendorId, productId, null, null, null, 0, null, port);
  }

}