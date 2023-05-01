package org.connectorio.addons.binding.mbus.config;

import org.connectorio.addons.binding.config.PollingConfiguration;
public class TcpBridgeConfig extends BridgeConfig {

  public String hostAddress;
  public int port;
  public int connectionTimeout;
}
