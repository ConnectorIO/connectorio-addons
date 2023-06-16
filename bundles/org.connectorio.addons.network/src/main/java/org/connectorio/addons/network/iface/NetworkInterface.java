package org.connectorio.addons.network.iface;

import java.util.List;
import org.connectorio.addons.network.Network;
import org.openhab.core.common.registry.Identifiable;

public interface NetworkInterface extends Identifiable<NetworkInterfaceUID> {

  NetworkInterfaceType getInterfaceType();

  List<Network> getNetworks();

}
