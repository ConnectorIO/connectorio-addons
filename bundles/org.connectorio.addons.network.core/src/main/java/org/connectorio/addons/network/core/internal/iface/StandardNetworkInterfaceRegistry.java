package org.connectorio.addons.network.core.internal.iface;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceProvider;
import org.connectorio.addons.network.iface.NetworkInterfaceRegistry;
import org.connectorio.addons.network.iface.NetworkInterfaceStateCallback;
import org.connectorio.addons.network.iface.NetworkInterfaceType;
import org.connectorio.addons.network.iface.NetworkInterfaceUID;
import org.openhab.core.common.registry.AbstractRegistry;
import org.openhab.core.common.registry.Provider;
import org.openhab.core.common.registry.Registry;
import org.osgi.service.component.annotations.Component;


@Component(service = {Registry.class, NetworkInterfaceRegistry.class})
public class StandardNetworkInterfaceRegistry extends AbstractRegistry<NetworkInterface, NetworkInterfaceUID, NetworkInterfaceProvider>
  implements NetworkInterfaceRegistry, NetworkInterfaceStateCallback {

  private final Set<NetworkInterfaceStateCallback> networkInterfaceListeners = new CopyOnWriteArraySet<>();

  public StandardNetworkInterfaceRegistry() {
    super(NetworkInterfaceProvider.class);
  }

  @Override
  public void addCentralNetworkInterfaceStateCallback(NetworkInterfaceStateCallback callback) {
    this.networkInterfaceListeners.add(callback);
  }

  @Override
  public void removeCentralNetworkInterfaceStateCallback(NetworkInterfaceStateCallback callback) {
    this.networkInterfaceListeners.remove(callback);
  }

  @Override
  public Collection<NetworkInterface> getAll(NetworkInterfaceType ... types) {
    List<NetworkInterfaceType> interfaceTypes = Arrays.asList(types);
    return getAll().stream().filter(networkInterface -> interfaceTypes.contains(networkInterface.getInterfaceType()))
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  protected void addProvider(Provider<NetworkInterface> provider) {
    ((NetworkInterfaceProvider) provider).addNetworkInterfaceStateCallback(this);
    super.addProvider(provider);
  }

  @Override
  protected void removeProvider(Provider<NetworkInterface> provider) {
    super.removeProvider(provider);
    ((NetworkInterfaceProvider) provider).removeNetworkInterfaceStateCallback(this);
  }

  // bridge between provider events and registry events
  @Override
  public void networkInterfaceUp(NetworkInterface networkInterface) {
    networkInterfaceListeners.forEach(listener -> listener.networkInterfaceUp(networkInterface));
  }

  @Override
  public void networkInterfaceDown(NetworkInterface networkInterface) {
    networkInterfaceListeners.forEach(listener -> listener.networkInterfaceDown(networkInterface));
  }

}
