package org.connectorio.addons.network.iface;

import java.util.Collection;
import org.openhab.core.common.registry.Registry;

public interface NetworkInterfaceRegistry extends Registry<NetworkInterface, NetworkInterfaceUID> {

  void addCentralNetworkInterfaceStateCallback(NetworkInterfaceStateCallback networkInterfaceStateCallback);
  void removeCentralNetworkInterfaceStateCallback(NetworkInterfaceStateCallback networkInterfaceStateCallback);

  Collection<NetworkInterface> getAll(NetworkInterfaceType ... types);

}
