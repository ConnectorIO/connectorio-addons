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
package org.connectorio.addons.io.transport.serial.config;

import org.openhab.core.io.transport.serial.SerialPort;

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