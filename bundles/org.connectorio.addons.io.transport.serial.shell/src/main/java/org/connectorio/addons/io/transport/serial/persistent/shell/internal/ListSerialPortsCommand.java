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
import java.util.concurrent.atomic.AtomicInteger;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which lists serial ports.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class ListSerialPortsCommand extends AbstractConsoleCommandExtension {

  private final SerialPortManager serialPortManager;

  @Activate
  public ListSerialPortsCommand(@Reference SerialPortManager serialPortManager) {
    super("co7io-list-serial-ports", "List serial ports detected by serial port manager.");
    this.serialPortManager = serialPortManager;
  }

  @Override
  public void execute(String[] args, Console console) {
    console.println("Detected ports:");
    AtomicInteger index = new AtomicInteger(1);
    serialPortManager.getIdentifiers().forEach(port -> {
      console.println((index.getAndIncrement()) + ") " + port.getName() + " owner: " + (port.isCurrentlyOwned() ? port.getCurrentOwner() : "none"));
    });
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-list-serial-ports");
  }

}
