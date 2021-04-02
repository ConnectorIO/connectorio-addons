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
package org.connectorio.addons.binding.bacnet.internal.handler.network.mstp;

import com.serotonin.bacnet4j.npdu.mstp.MasterNode;
import com.serotonin.bacnet4j.npdu.mstp.MstpNetwork;
import com.serotonin.bacnet4j.util.sero.SerialPortWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.code_house.bacnet4j.wrapper.mstp.MstpNetworkBuilder;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;

public class ManagedMstpNetworkBuilder extends MstpNetworkBuilder {

  private final SerialPortManager serialPortManager;

  public ManagedMstpNetworkBuilder(SerialPortManager serialPortManager) {
    this.serialPortManager = serialPortManager;
  }

  @Override
  public MstpNetwork build() throws Exception {
    SerialPortIdentifier identifier = serialPortManager.getIdentifier(getSerialPort());

    MasterNode node = new MasterNode(new ManagedSerialPort(identifier, getBaud(), getDataBits(), getStopBits(), getParity()),
      (byte)this.getStation(), 2);
    node.setMaxInfoFrames(5);
    node.setUsageTimeout(100);
    return new MstpNetwork(node, 0);
  }

  static class ManagedSerialPort extends SerialPortWrapper {

    private final SerialPortIdentifier identifier;
    private final int baud;
    private final int dataBits;
    private final int stopBits;
    private final int parity;
    private SerialPort port;

    ManagedSerialPort(SerialPortIdentifier port, int baud, int dataBits, int stopBits, int parity) {
      this.identifier = port;
      this.baud = baud;
      this.dataBits = dataBits;
      this.stopBits = stopBits;
      this.parity = parity;
    }

    @Override
    public void close() throws Exception {
      if (port != null) {
        port.close();
      }
    }

    @Override
    public void open() throws Exception {
      port = identifier.open(BACnetBindingConstants.BINDING_ID, 2000);
      port.setSerialPortParams(baud, dataBits, stopBits, parity);
    }

    @Override
    public InputStream getInputStream() {
      if (port != null) {
        try {
          return port.getInputStream();
        } catch (IOException e) {
          throw new RuntimeException("Could not open input stream for port " + identifier.getName(), e);
        }
      }
      return null;
    }

    @Override
    public OutputStream getOutputStream() {
      if (port != null) {
        try {
          return port.getOutputStream();
        } catch (IOException e) {
          throw new RuntimeException("Could not open output stream for port " + identifier.getName(), e);
        }
      }
      return null;
    }

    @Override
    public String getCommPortId() {
      return identifier.getName();
    }
  }
}
