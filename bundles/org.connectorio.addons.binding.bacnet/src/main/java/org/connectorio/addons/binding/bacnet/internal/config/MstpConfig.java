/*
 * Copyright (C) 2019-2020 ConnectorIO sp. z o.o.
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
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.config;

import org.eclipse.smarthome.io.transport.serial.SerialPort;

public class MstpConfig extends BACnetConfig {

  public int station;
  public int localDeviceId;

  public String serialPort;
  public int baudRate;
  public Parity parity;

  public enum Parity {
    P8N1 (SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1),
    P8N2 (SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_2),
    P8E1 (SerialPort.DATABITS_8, SerialPort.PARITY_EVEN, SerialPort.STOPBITS_1),
    P8O1 (SerialPort.DATABITS_8, SerialPort.PARITY_ODD, SerialPort.STOPBITS_1);

    private final int dataBits;
    private final int parity;
    private final int stopBits;

    Parity(int dataBits, int parity, int stopBits) {
      this.dataBits = dataBits;
      this.parity = parity;
      this.stopBits = stopBits;
    }

    public int getDataBits() {
      return dataBits;
    }

    public int getParity() {
      return parity;
    }

    public int getStopBits() {
      return stopBits;
    }

  }

}
