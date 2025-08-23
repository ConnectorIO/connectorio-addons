package org.connectorio.addons.binding.canbus.config.channel;

public class ByteChannelConfig extends BaseOffsetConfig implements LengthConfig {

  public int length;

  @Override
  public int length() {
    return length;
  }

}
