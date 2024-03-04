package org.connectorio.addons.binding.mbus.config;

import org.connectorio.addons.binding.config.PollingConfiguration;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.SecondaryAddress;

public class DeviceConfig extends PollingConfiguration {

  // primary address
  public Integer address;

  public Integer serialNumber;
  public String manufacturerId;
  public Integer version;
  public DeviceType deviceType;

  public boolean discoverChannels = true;

  public SecondaryAddress getSecondaryAddress() {
    if (serialNumber == null || manufacturerId == null || version == null || deviceType == null) {
      return null;
    }

    // create m-bus address using long variant
    return SecondaryAddress.newFromManufactureId(
      bcd(serialNumber),
      manufacturerId,
      version.byteValue(),
      Integer.valueOf(deviceType.getId()).byteValue(),
      true
    );
  }

  private static byte[] bcd(int value){
    byte[] bcd = new byte[4];
    for (int index = 0; index < 4; index++){
      bcd[index] = (byte) (value % 10);
      value /= 10;
      bcd[index] |= (byte) ((value % 10) << 4);
      value /= 10;
    }
    return bcd;
  }

}
