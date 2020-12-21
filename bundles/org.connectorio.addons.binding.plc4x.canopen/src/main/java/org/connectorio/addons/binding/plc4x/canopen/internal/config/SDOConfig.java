package org.connectorio.addons.binding.plc4x.canopen.internal.config;

import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.config.PollingConfiguration;

public class SDOConfig extends PollingConfiguration {

  short index;
  short subIndex;
  CANOpenDataType readType;

}
