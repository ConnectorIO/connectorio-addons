package org.connectorio.addons.network.ip;

import org.connectorio.addons.network.Network;

public interface IpNetwork extends Network {

  String getBroadcastAddress();

  String getAddress();

}
