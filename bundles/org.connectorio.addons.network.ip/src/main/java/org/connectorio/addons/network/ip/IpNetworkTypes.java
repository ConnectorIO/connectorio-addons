package org.connectorio.addons.network.ip;

import org.connectorio.addons.network.NetworkType;

public class IpNetworkTypes {

  public final static NetworkType IPv4 = new NetworkType() {
    @Override
    public String getType() {
      return "ipv4";
    }
  };

  public final static NetworkType IPv6 = new NetworkType() {
    @Override
    public String getType() {
      return "ipv6";
    }
  };

}
