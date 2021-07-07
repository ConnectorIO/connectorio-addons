/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later OR commercial
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
