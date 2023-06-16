package org.connectorio.addons.network.iface;

import org.openhab.core.common.registry.Provider;

public interface NetworkInterfaceProvider extends Provider<NetworkInterface> {

  void addNetworkInterfaceStateCallback(NetworkInterfaceStateCallback networkInterfaceStateCallback);
  void removeNetworkInterfaceStateCallback(NetworkInterfaceStateCallback networkInterfaceStateCallback);

}
