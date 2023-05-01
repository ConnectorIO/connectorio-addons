package org.connectorio.addons.binding.mbus.config;

import org.connectorio.addons.io.transport.serial.config.DataBits;
import org.connectorio.addons.io.transport.serial.config.FlowControl;
import org.connectorio.addons.io.transport.serial.config.Parity;
import org.connectorio.addons.io.transport.serial.config.Rts;
import org.connectorio.addons.io.transport.serial.config.SerialPortConfig;
import org.connectorio.addons.io.transport.serial.config.StopBits;

public class SerialBridgeConfig extends BridgeConfig implements SerialPortConfig {

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
