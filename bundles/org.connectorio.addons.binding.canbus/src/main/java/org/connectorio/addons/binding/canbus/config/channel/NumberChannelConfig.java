package org.connectorio.addons.binding.canbus.config.channel;

import org.apache.plc4x.java.genericcan.readwrite.GenericCANDataType;

public class NumberChannelConfig extends BaseOffsetConfig implements LengthConfig {

  public int length;

  public GenericCANDataType type;

  @Override
  public int length() {
    return length;
  }

}
