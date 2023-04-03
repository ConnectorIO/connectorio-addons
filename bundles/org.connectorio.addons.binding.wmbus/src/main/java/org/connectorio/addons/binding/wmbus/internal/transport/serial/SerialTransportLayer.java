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
package org.connectorio.addons.binding.wmbus.internal.transport.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openmuc.jmbus.transportlayer.TransportLayer;

public class SerialTransportLayer implements TransportLayer {

  private final SerialPortIdentifier portIdentifier;
  private final SerialPortManager serialPortManager;
  private final int baudRate;
  private final int dataBits;
  private final int stopBits;
  private final int parity;
  private final int flowControl;
  private final boolean rts;
  private SerialPort serialPort;
  private int timeout;

  public SerialTransportLayer(SerialPortIdentifier portIdentifier, SerialPortManager serialPortManager, int baudRate, int dataBits, int stopBits, int parity, int flowControl, boolean rts) {
    this.portIdentifier = portIdentifier;
    this.serialPortManager = serialPortManager;
    this.baudRate = baudRate;
    this.dataBits = dataBits;
    this.stopBits = stopBits;
    this.parity = parity;
    this.flowControl = flowControl;
    this.rts = rts;
  }

  @Override
  public void open() throws IOException {
    try {
      serialPort = portIdentifier.open("connectorio-wmbus", timeout);
      serialPort.setFlowControlMode(flowControl);
      serialPort.setRTS(rts);
      serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
    } catch (PortInUseException | UnsupportedCommOperationException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void close() {
    if (serialPort != null) {
      serialPort.close();
    }
  }

  @Override
  public DataOutputStream getOutputStream() {
    try {
      return new DataOutputStream(serialPort.getOutputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DataInputStream getInputStream() {
    try {
      return new DataInputStream(serialPort.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isClosed() {
    return serialPort == null;
  }

  @Override
  public void setTimeout(int timeout) throws IOException {
    if (serialPort == null) {
      return;
    }
    try {
      if (timeout == 0) {
        serialPort.disableReceiveTimeout();
        return;
      }
      serialPort.enableReceiveTimeout(timeout);
    } catch (UnsupportedCommOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getTimeout() throws IOException {
    return this.timeout;
  }

}