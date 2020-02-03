package org.connectorio.binding.bacnet.internal.handler.network;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.ip.BacNetIpClient;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.binding.bacnet.internal.config.Ipv4Config;
import org.connectorio.binding.bacnet.internal.discovery.BACnetDeviceDiscoveryService;
import org.connectorio.binding.base.handler.polling.common.BasePollingBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;

public class BACnetIpv4BridgeHandler extends BasePollingBridgeHandler<Ipv4Config> implements BACnetNetworkBridgeHandler<Ipv4Config> {

  private CompletableFuture<BacNetClient> clientFuture = new CompletableFuture<>();
  private BacNetClient client;

  /**
   * Creates a new instance of this class for the {@link Bridge}.
   *
   * @param bridge the bridge that should be handled, not null
   */
  public BACnetIpv4BridgeHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  public void initialize() {
    IpNetworkBuilder builder = getBridgeConfig().map(config -> {
      return new IpNetworkBuilder()
        .withBroadcast(config.broadcastAddress, 24)
        //.withLocalBindAddress(config.localBindAddress)
        .withPort(config.port)
        .withLocalNetworkNumber(config.localNetworkNumber)
        .withReuseAddress(true)
        ;
    }).orElse(new IpNetworkBuilder());

    clientFuture.handleAsync((c, e) -> {
      if (e != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
      } else {
        updateStatus(ThingStatus.ONLINE);
      }
      return null;
    }, scheduler);
    clientFuture.thenAcceptAsync(c -> this.client = c, scheduler);

    scheduler.submit(() -> {
      BacNetIpClient cli = new BacNetIpClient(builder.build(), getLocalDeviceId().orElse(1339));
      cli.start();
      clientFuture.complete(cli);
    });
  }

  @Override
  public void dispose() {
    if (client != null) {
      client.stop();
    }

    clientFuture.cancel(true);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  public CompletableFuture<BacNetClient> getClient() {
    return clientFuture;
  }

  @Override
  public Optional<Integer> getNetworkNumber() {
    return getBridgeConfig().map(cfg -> cfg.localNetworkNumber);
  }

  public Optional<Integer> getLocalDeviceId() {
    return getBridgeConfig().map(cfg -> cfg.localDeviceId);
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(BACnetDeviceDiscoveryService.class);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

}
