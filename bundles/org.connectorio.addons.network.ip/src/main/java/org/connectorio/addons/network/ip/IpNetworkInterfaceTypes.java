package org.connectorio.addons.network.ip;

import org.connectorio.addons.network.iface.NetworkInterfaceType;

public class IpNetworkInterfaceTypes {
  public static final NetworkInterfaceType LOOPBACK = new NetworkInterfaceType() {
    @Override
    public String getType() {
      return "loopback";
    }
  };

  public static final NetworkInterfaceType IP = new NetworkInterfaceType() {
    @Override
    public String getType() {
      return "IP";
    }
  };

}
