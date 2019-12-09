package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.plc4x.java.ads.connection.AdsAbstractPlcConnection;
import org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffBridgeConfiguration;
import org.connectorio.binding.plc4x.shared.handler.SharedPlc4xBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top level type unifying handling of Beckhoff bridges.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public abstract class BeckhoffBridgeHandler<T extends AdsAbstractPlcConnection, C extends BeckhoffBridgeConfiguration> extends
    SharedPlc4xBridgeHandler<T, C> {

  private final Logger logger = LoggerFactory.getLogger(BeckhoffBridgeHandler.class);
  private CompletableFuture<T> initializer;

  public BeckhoffBridgeHandler(Bridge thing) {
    super(thing);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.UNKNOWN);

    initializer = new CompletableFuture<>();
    Runnable connectionTask = createInitializer(initializer);
    scheduler.submit(connectionTask);
  }

  @Override
  public CompletableFuture<T> getInitializer() {
    return initializer;
  }

  protected abstract Runnable createInitializer(CompletableFuture<T> initializer);

  @Override
  public T getConnection() {
    try {
      return initializer.get(5, TimeUnit.SECONDS);
    } catch (InterruptedException | TimeoutException | ExecutionException e) {
      logger.warn("Could not obtain connection", e);
      return null;
    }
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BeckhoffBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
