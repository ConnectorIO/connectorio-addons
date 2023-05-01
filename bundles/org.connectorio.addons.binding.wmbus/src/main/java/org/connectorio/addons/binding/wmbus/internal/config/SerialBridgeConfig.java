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
package org.connectorio.addons.binding.wmbus.internal.config;


import org.connectorio.addons.io.transport.serial.config.DataBits;
import org.connectorio.addons.io.transport.serial.config.FlowControl;
import org.connectorio.addons.io.transport.serial.config.Parity;
import org.connectorio.addons.io.transport.serial.config.Rts;
import org.connectorio.addons.io.transport.serial.config.SerialPortConfig;
import org.connectorio.addons.io.transport.serial.config.StopBits;
import org.openmuc.jmbus.wireless.WMBusMode;

public class SerialBridgeConfig extends BridgeConfig implements SerialPortConfig {

  public WMBusMode mode;

  public String serialPort;
  public Integer baudRate;
  public DataBits dataBits;
  public StopBits stopBits;
  public Parity parity;
  public FlowControl flowControl;

  public Rts rts;

  @Override
  public String getSerialPort() {
    return serialPort;
  }

  @Override
  public Integer getBaudRate() {
    return baudRate;
  }

  @Override
  public DataBits getDataBits() {
    return dataBits;
  }

  @Override
  public StopBits getStopBits() {
    return stopBits;
  }

  @Override
  public Parity getParity() {
    return parity;
  }

  @Override
  public FlowControl getFlowControl() {
    return flowControl;
  }

  public Rts getRts() {
    return rts;
  }

}
