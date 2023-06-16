package org.connectorio.addons.network.ip;

import org.connectorio.addons.network.iface.NetworkInterface;

public interface IpNetworkInterface extends NetworkInterface {

  String getMac();

  String getName();

  String getDisplayName();

  boolean isUp();

}
