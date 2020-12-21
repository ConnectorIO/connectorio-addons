package org.connectorio.addons.binding.plc4x.canopen.internal.handler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.plc4x.canopen.CANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.handler.CANopenBridgeHandler;
import org.connectorio.addons.binding.plc4x.canopen.config.CANopenNodeConfig;
import org.connectorio.addons.binding.plc4x.handler.Plc4xBridgeHandler;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;

public class CANOpenGenericBridgeHandler extends PollingPlc4xBridgeHandler<PlcConnection, CANopenNodeConfig> {

  public CANOpenGenericBridgeHandler(Bridge thing) {
    super(thing);
  }

  @Override
  protected CompletableFuture<PlcConnection> getPlcConnection() {
    return CompletableFuture.supplyAsync(() -> getBridgeHandler()
      .map(Plc4xBridgeHandler::getConnection)
      .orElseThrow(() -> new IllegalStateException("Not ready")).join()
    );
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.ONLINE);

  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  protected Long getDefaultPollingInterval() {
    return CANopenBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  public Optional<CANopenBridgeHandler<?>> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(CANopenBridgeHandler.class::isInstance)
      .map(handler -> (CANopenBridgeHandler<?>) handler);
  }

}
