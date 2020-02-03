package org.connectorio.binding.bacnet.internal.discovery;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.net.NetworkAddressChangeListener;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = {DiscoveryService.class}, configurationPid = "discovery.bacnet.interface")
public class BACnetInterfaceDiscoveryService extends AbstractDiscoveryService implements NetworkAddressChangeListener {

  private final Logger logger = LoggerFactory.getLogger(BACnetInterfaceDiscoveryService.class);
  private final NetworkAddressService networkAddressService;

  @Activate
  public BACnetInterfaceDiscoveryService(@Reference NetworkAddressService networkAddressService)
    throws IllegalArgumentException {
    super(Collections.singleton(BACnetBindingConstants.IPV4_BRIDGE_THING_TYPE), 60, true);
    this.networkAddressService = networkAddressService;

    networkAddressService.addNetworkAddressChangeListener(this);
  }

  @Override
  @Deactivate
  public void deactivate() {
    logger.debug("Deactivating BACnet network interface discovery service");
    networkAddressService.removeNetworkAddressChangeListener(this);
  }

  @Override
  protected void startScan() {
    Collection<CidrAddress> addresses = NetUtil.getAllInterfaceAddresses();
    discover(addresses);
  }

  @Override
  public void onChanged(List<CidrAddress> added, List<CidrAddress> removed) {
    if (!removed.isEmpty()) {
      removeOlderResults(getTimestampOfLastScan());
    }

    discover(added);
  }

  private void discover(Collection<CidrAddress> addresses) {
    for (CidrAddress addr : addresses) {
      InetAddress address = addr.getAddress();
      if (address instanceof Inet4Address) {
        String broadcastAddress = NetUtil.getIpv4NetBroadcastAddress(address.getHostAddress(), (short) addr.getPrefix());

        DiscoveryResult network = DiscoveryResultBuilder
          .create(new ThingUID(BACnetBindingConstants.IPV4_BRIDGE_THING_TYPE, broadcastAddress.replace(".", "_")))
          .withLabel("BACnet IPv4 network " + address.getHostAddress())
          //.withProperty("localBindAddress", address.getHostAddress())
          .withProperty("broadcastAddress", broadcastAddress)
          .build();
        thingDiscovered(network);
      }
    }
  }

}