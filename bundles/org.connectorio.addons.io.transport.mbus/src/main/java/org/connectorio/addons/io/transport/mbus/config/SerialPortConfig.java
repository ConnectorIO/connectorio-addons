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
package org.connectorio.addons.io.transport.mbus.config;

import org.openhab.core.io.transport.serial.SerialPort;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusManufacturer;
import org.openmuc.jmbus.wireless.WMBusMode;

public class SerialPortConfig {

  public String serialPort;
  public Integer baudRate;
  public DataBits dataBits;
  public StopBits stopBits;
  public Parity parity;
  public FlowControl flowControl;
  public Boolean rts;

  public enum DataBits {
    DATABITS_5 (SerialPort.DATABITS_5),
    DATABITS_6 (SerialPort.DATABITS_6),
    DATABITS_7 (SerialPort.DATABITS_7),
    DATABITS_8 (SerialPort.DATABITS_8);

    private int dataBits;

    DataBits(int dataBits) {
      this.dataBits = dataBits;
    }

    public int getDataBits() {
      return dataBits;
    }
  }

  public enum StopBits {
    STOPBITS_1 (SerialPort.STOPBITS_1),
    STOPBITS_2 (SerialPort.STOPBITS_2),
    STOPBITS_1_5 (SerialPort.STOPBITS_1_5);

    private int stopBits;

    StopBits(int stopBits) {
      this.stopBits = stopBits;
    }

    public int getStopBits() {
      return stopBits;
    }
  }

  public enum Parity {
    PARITY_NONE (SerialPort.PARITY_NONE),
    PARITY_ODD (SerialPort.PARITY_ODD),
    PARITY_EVEN (SerialPort.PARITY_EVEN),
    PARITY_MARK (SerialPort.PARITY_MARK),
    PARITY_SPACE (SerialPort.PARITY_SPACE);

    private int parity;

    Parity(int parity) {
      this.parity = parity;
    }

    public int getParity() {
      return parity;
    }
  }

  public enum FlowControl {
    FLOWCONTROL_NONE (SerialPort.FLOWCONTROL_NONE),
    FLOWCONTROL_RTSCTS_IN (SerialPort.FLOWCONTROL_RTSCTS_IN),
    FLOWCONTROL_RTSCTS_OUT (SerialPort.FLOWCONTROL_RTSCTS_OUT),
    FLOWCONTROL_XONXOFF_IN (SerialPort.FLOWCONTROL_XONXOFF_IN),
    FLOWCONTROL_XONXOFF_OUT (SerialPort.FLOWCONTROL_XONXOFF_OUT);

    private final int flowControl;

    FlowControl(int flowControl) {
      this.flowControl = flowControl;
    }

    public int getFlowControl() {
      return flowControl;
    }
  }
}
