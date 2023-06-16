package org.connectorio.addons.network.iface;

public interface NetworkInterfaceStateCallback {

  void networkInterfaceUp(NetworkInterface networkInterface);

  void networkInterfaceDown(NetworkInterface networkInterface);

}
