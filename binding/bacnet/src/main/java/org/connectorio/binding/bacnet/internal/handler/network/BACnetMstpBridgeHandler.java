package org.connectorio.binding.bacnet.internal.handler.network;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.mstp.BacNetMstpClient;
import org.code_house.bacnet4j.wrapper.mstp.JsscMstpNetworkBuilder;
import org.code_house.bacnet4j.wrapper.mstp.MstpNetworkBuilder;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.binding.bacnet.internal.config.MstpConfig;
import org.connectorio.binding.bacnet.internal.config.MstpConfig.Parity;
import org.connectorio.binding.bacnet.internal.discovery.BACnetMstpDeviceDiscoveryService;
import org.connectorio.binding.bacnet.internal.handler.network.mstp.ManagedMstpNetworkBuilder;
import org.connectorio.binding.base.handler.polling.common.BasePollingBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;

public class BACnetMstpBridgeHandler extends BasePollingBridgeHandler<MstpConfig> implements BACnetNetworkBridgeHandler<MstpConfig> {

  private final SerialPortManager serialPortManager;

  private CompletableFuture<BacNetClient> clientFuture = new CompletableFuture<>();
  private BacNetClient client;

  /**
   * Creates a new instance of this class for the {@link Bridge}.
   *
   * @param bridge the bridge that should be handled, not null
   * @param serialPortManager
   */
  public BACnetMstpBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
    super(bridge);
    this.serialPortManager = serialPortManager;
  }

  @Override
  public void initialize() {
    MstpNetworkBuilder builder = getBridgeConfig().map(config -> {
      Parity parity = config.parity;
      return new ManagedMstpNetworkBuilder(serialPortManager)
        .withSerialPort(config.serialPort)
        .withStation(config.station)
        .withBaud(config.baudRate)
        .withDataBits((short) parity.getDataBits())
        .withParity((short) parity.getParity())
        .withStopBits((short) parity.getStopBits());
    }).orElse(new JsscMstpNetworkBuilder());

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
      try {
        BacNetMstpClient cli = new BacNetMstpClient(builder.build(), getLocalDeviceId().orElse(1339));
        cli.start();
        clientFuture.complete(cli);
      } catch (Exception e) {
        clientFuture.completeExceptionally(e);
      }
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
    return Collections.singleton(BACnetMstpDeviceDiscoveryService.class);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

}
