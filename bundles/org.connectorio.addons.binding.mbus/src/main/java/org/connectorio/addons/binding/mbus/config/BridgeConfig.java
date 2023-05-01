package org.connectorio.addons.binding.mbus.config;

import org.connectorio.addons.binding.config.PollingConfiguration;

public abstract class BridgeConfig extends PollingConfiguration {

  public DiscoveryMethod discoveryMethod;
  public String wildcardMask;

  public int discoveryTimeToLive;

}
