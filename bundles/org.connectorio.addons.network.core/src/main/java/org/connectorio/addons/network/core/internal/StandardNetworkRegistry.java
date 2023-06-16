package org.connectorio.addons.network.core.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.NetworkProvider;
import org.connectorio.addons.network.NetworkRegistry;
import org.connectorio.addons.network.NetworkType;
import org.connectorio.addons.network.NetworkUID;
import org.openhab.core.common.registry.AbstractRegistry;
import org.openhab.core.common.registry.Registry;
import org.osgi.service.component.annotations.Component;

@Component(service = {Registry.class, NetworkRegistry.class})
public class StandardNetworkRegistry extends AbstractRegistry<Network, NetworkUID, NetworkProvider>
  implements NetworkRegistry {

  public StandardNetworkRegistry() {
    super(NetworkProvider.class);
  }

  public Stream<Network> getNetworksOfType(NetworkType... type) {
    Set<NetworkType> types = new HashSet<>(Arrays.asList(type));
    return getAll().stream().filter(network -> types.contains(network.getType()));
  }

}
