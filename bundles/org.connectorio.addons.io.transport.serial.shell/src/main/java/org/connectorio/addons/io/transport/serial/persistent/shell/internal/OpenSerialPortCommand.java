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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which lists serial ports.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class OpenSerialPortCommand extends AbstractConsoleCommandExtension {

  private final SerialPortManager serialPortManager;

  @Activate
  public OpenSerialPortCommand(@Reference SerialPortManager serialPortManager) {
    super("co7io-open-serial-port", "Attempts to open and close serial port over serial port manager apis.");
    this.serialPortManager = serialPortManager;
  }

  @Override
  public void execute(String[] args, Console console) {
    if (args.length != 1) {
      console.println("Port index is necessary");
      return;
    }

    console.println("Attempting to open port: " + args[0]);
    int portIndex = Integer.parseInt(args[0]);
    AtomicInteger index = new AtomicInteger(1);
    Optional<SerialPortIdentifier> identifier = serialPortManager.getIdentifiers().filter(port -> portIndex == index.getAndIncrement()).findFirst();

    if (identifier.isPresent()) {
      SerialPortIdentifier portIdentifier = identifier.get();
      SerialPort port = null;
      try {
        port = portIdentifier.open("test", 10);
      } catch (PortInUseException e) {
        console.println("Could not open port: " + e.getMessage());
        e.printStackTrace(); // goes to stderr!
      } finally {
        if (port != null) {
          port.close();
        }
      }
    } else {
      console.println("Port with given index not found");
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-open-serial-port <port-index>");
  }

}
