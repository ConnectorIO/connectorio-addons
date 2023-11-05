package org.connectorio.addons.binding.canbus.config.channel;

public class BaseOffsetConfig implements OffsetConfig {

  public int offset;

  @Override
  public int offset() {
    return offset;
  }

}
