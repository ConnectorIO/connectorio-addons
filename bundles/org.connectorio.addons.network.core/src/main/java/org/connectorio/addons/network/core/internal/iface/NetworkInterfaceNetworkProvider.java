package org.connectorio.addons.network.core.internal.iface;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.NetworkUID;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.openhab.core.common.registry.ProviderChangeListener;

import org.connectorio.addons.network.NetworkProvider;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.osgi.service.component.annotations.Component;

@Component(service = NetworkInterfaceNetworkProvider.class)
public class NetworkInterfaceNetworkProvider implements NetworkProvider, RegistryChangeListener<NetworkInterface> {

  private final Set<ProviderChangeListener<Network>> listeners = new CopyOnWriteArraySet<>();
  private final Map<NetworkInterface, List<Network>> networkMap = new ConcurrentHashMap<>();

  @Override
  public void addProviderChangeListener(ProviderChangeListener<Network> listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<Network> listener) {
    this.listeners.remove(listener);
  }

  @Override
  public Collection<Network> getAll() {
    return networkMap.values().stream()
      .flatMap(List::stream)
      .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public void added(NetworkInterface element) {
    List<Network> networks = element.getNetworks();
    this.networkMap.put(element, networks);
    broadcast(networks, (listener, network) -> listener.added(this, network));
  }

  @Override
  public void removed(NetworkInterface element) {
    List<Network> networks = this.networkMap.remove(element);
    broadcast(networks, (listener, network) -> listener.removed(this, network));
  }

  @Override
  public void updated(NetworkInterface oldElement, NetworkInterface element) {
    List<Network> knownNetworks = networkMap.remove(oldElement);
    List<Network> newNetworks = element.getNetworks();
    networkMap.put(element, element.getNetworks());

    // calculate difference by removing already registered networks from new networks
    // all networks which are found in addedNetwork list are new, as they were not present
    // in earlier representation of NetworkInterface
    Map<NetworkUID, Network> addedNetworks = new HashMap<>();
    Map<NetworkUID, Entry<Network, Network>> changedNetworks = new HashMap<>();
    Map<NetworkUID, Network> removedNetworks = new HashMap<>();
    for (Network network : newNetworks) {
      if (!knownNetworks.contains(network)) {
        addedNetworks.put(network.getUID(), network);
      }
    }
    for (Network network : knownNetworks) {
      if (!newNetworks.contains(network)) {
        removedNetworks.put(network.getUID(), network);
      }
    }
    for (Entry<NetworkUID, Network> entry : addedNetworks.entrySet()) {
      NetworkUID id = entry.getKey();
      if (removedNetworks.containsKey(id)) {
        Entry<Network, Network> entry1 = Map.entry(removedNetworks.remove(id), entry.getValue());
        changedNetworks.put(id, entry1);
      }
    }
    for (Entry<NetworkUID, Entry<Network, Network>> changedEntry : changedNetworks.entrySet()) {
      addedNetworks.remove(changedEntry.getKey());
      for (ProviderChangeListener<Network> listener : listeners) {
        listener.updated(this, changedEntry.getValue().getKey(), changedEntry.getValue().getValue());
      }
    }

    broadcast(removedNetworks.values(), (listener, network) -> listener.removed(this, network));
    broadcast(addedNetworks.values(), (listener, network) -> listener.added(this, network));
  }

  private void broadcast(Collection<Network> networks, BiConsumer<ProviderChangeListener<Network>, Network> consumer) {
    if (networks != null && !networks.isEmpty()) {
      for (Network network : networks) {
        listeners.forEach(listener -> consumer.accept(listener, network));
      }
    }
  }

}
