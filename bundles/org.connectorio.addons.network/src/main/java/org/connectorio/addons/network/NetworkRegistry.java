package org.connectorio.addons.network;

import java.util.stream.Stream;
import org.openhab.core.common.registry.Registry;

public interface NetworkRegistry extends Registry<Network, NetworkUID> {

  Stream<Network> getNetworksOfType(NetworkType ... type);

}
