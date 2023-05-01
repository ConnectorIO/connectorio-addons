package org.connectorio.addons.binding.mbus.config;

import org.connectorio.addons.binding.config.PollingConfiguration;
import org.openmuc.jmbus.DeviceType;

public class DeviceConfig extends PollingConfiguration {

  // primary address
  public Integer address;

  public int serialNumber;
  public String manufacturerId;
  public int version;
  public DeviceType deviceType;

  public boolean discoverChannels = true;


}
