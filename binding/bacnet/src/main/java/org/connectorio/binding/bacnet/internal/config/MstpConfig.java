package org.connectorio.binding.bacnet.internal.config;

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
