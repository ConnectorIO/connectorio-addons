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
    SerialPort port = identifier.open(BACnetBindingConstants.BINDING_ID, 2000);
    port.setSerialPortParams(getBaud(), getDataBits(), getParity(), getStopBits());

    MasterNode node = new MasterNode(this.getSerialPort(), port.getInputStream(), port.getOutputStream(), (byte)this.getStation(), 2);
    node.setMaxInfoFrames(5);
    node.setUsageTimeout(100);
    return new MstpNetwork(node, 0);
  }
}
