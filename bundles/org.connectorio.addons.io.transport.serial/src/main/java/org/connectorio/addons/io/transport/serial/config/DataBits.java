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
