package org.connectorio.binding.bacnet.internal.handler.network.mstp;

import com.serotonin.bacnet4j.npdu.mstp.MasterNode;
import com.serotonin.bacnet4j.npdu.mstp.MstpNetwork;
import org.code_house.bacnet4j.wrapper.mstp.MstpNetworkBuilder;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;

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
