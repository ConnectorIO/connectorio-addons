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
