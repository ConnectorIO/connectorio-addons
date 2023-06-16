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
package org.connectorio.addons.io.transport.serial.purejavacomm.internal;

import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import purejavacomm.CommPortIdentifier;

/**
 * Serial port identifier bound to library.
 */
public class PureJavaCommSerialPortIdentifier implements SerialPortIdentifier {

  private final CommPortIdentifier identifier;

  public PureJavaCommSerialPortIdentifier(CommPortIdentifier identifier) {
    this.identifier = identifier;
  }

  @Override
  public String getName() {
    return identifier.getName();
  }

  @Override
  public SerialPort open(String owner, int timeout) throws PortInUseException {
    try {
      return new PureJavaCommSerialPort((purejavacomm.SerialPort) identifier.open(owner, timeout));
    } catch (purejavacomm.PortInUseException e) {
      throw new PortInUseException(e);
    }
  }

  @Override
  public boolean isCurrentlyOwned() {
    return identifier.isCurrentlyOwned();
  }

  @Override
  public String getCurrentOwner() {
    return identifier.getCurrentOwner();
  }

  public String toString() {
    return getName() + " (pure java comm)";
  }
}
