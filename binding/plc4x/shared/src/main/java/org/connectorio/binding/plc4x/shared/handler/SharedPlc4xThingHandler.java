package org.connectorio.binding.plc4x.shared.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.polling.common.BasePollingThingHandler;
import org.connectorio.binding.plc4x.shared.config.CommonChannelConfiguration;
import org.connectorio.binding.plc4x.shared.handler.task.ReadTask;
import org.connectorio.binding.plc4x.shared.handler.task.WriteTask;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SharedPlc4xThingHandler<T extends PlcConnection, B extends SharedPlc4xBridgeHandler<T, ?>, C extends PollingConfiguration> extends
  BasePollingThingHandler<B, C> implements ThingHandler {

  protected final Map<ChannelUID, ScheduledFuture> futures = new ConcurrentHashMap<>();
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public SharedPlc4xThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    getBridgeHandler().map(SharedPlc4xBridgeHandler::getInitializer)
      .map(future -> future.whenCompleteAsync(this::connect, this.scheduler));
  }

  private void connect(T connection, Throwable e) {
    if (e != null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
      return;
    }

    List<String> configErrors = new ArrayList<>();
    for (Channel channel : thing.getChannels()) {
      final ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
      if (channelTypeUID == null) {
        logger.warn("Channel {} has no type", channel.getLabel());
        continue;
      }
      final CommonChannelConfiguration channelConfig = channel.getConfiguration().as(
        CommonChannelConfiguration.class);

      try {
        Long cycleTime = channelConfig.refreshInterval == null ? getRefreshInterval()
          : channelConfig.refreshInterval;
        ScheduledFuture<?> future = scheduler
          .scheduleAtFixedRate(new ReadTask(connection, getCallback(), channel), 0,
            cycleTime, TimeUnit.MILLISECONDS);
        futures.put(channel.getUID(), future);
      } catch (PlcRuntimeException er) {
        logger.warn("Channel configuration error", er);
        configErrors.add(channel.getLabel() + ": " + er.getMessage());
      }
    }

    // If some channels could not start up, put the entire thing offline and display the channels
    // in question to the user.
    if (!configErrors.isEmpty()) {
      clearTasks();
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        "Errors in field configuration: "
          + configErrors.stream().collect(Collectors.joining(",")));
      return;
    }

    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    Channel channel = getThing().getChannel(channelUID);

    if (RefreshType.REFRESH == command) {
      getPlcConnection().ifPresent(connection -> scheduler.submit(new ReadTask(connection,
          getCallback(), channel)));
    } else {
      getPlcConnection()
          .ifPresent(connection -> scheduler.submit(new WriteTask(connection, channel, command)));
    }
  }

  protected Optional<T> getPlcConnection() {
    return getBridgeHandler().map(SharedPlc4xBridgeHandler::getConnection);
  }

  @Override
  public void dispose() {
    // clean up tasks which should not remain in scheduler pool
    clearTasks();
  }

  private void clearTasks() {
    futures.forEach((k, v) -> v.cancel(false));
  }

}