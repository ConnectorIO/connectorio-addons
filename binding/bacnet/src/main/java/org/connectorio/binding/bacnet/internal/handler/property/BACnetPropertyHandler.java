package org.connectorio.binding.bacnet.internal.handler.property;

import com.serotonin.bacnet4j.obj.BACnetObject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.api.Property;
import org.code_house.bacnet4j.wrapper.api.Type;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.binding.bacnet.internal.config.ChannelConfig;
import org.connectorio.binding.bacnet.internal.config.ObjectConfig;
import org.connectorio.binding.base.handler.PollingHandler;
import org.connectorio.binding.base.handler.polling.common.BasePollingThingHandler;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;

public abstract class BACnetPropertyHandler<T extends BACnetObject, B extends BACnetDeviceBridgeHandler<?, ?>, C extends ObjectConfig>
  extends BasePollingThingHandler<B, C> {

  private final Type type;
  private Property property;
  private Future<?> reader;

  public BACnetPropertyHandler(Thing thing, Type type) {
    super(thing);
    this.type = type;
  }

  @Override
  public void initialize() {
    Device device = getBridgeHandler().map(b -> b.getDevice()).orElse(null);
    int instance = getThingConfig().map(c -> c.instance).orElseThrow(() -> new IllegalStateException("Undefined instance number"));

    if (device != null) {
      this.property = new Property(device, instance, type);

      updateStatus(ThingStatus.ONLINE);
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Link object to device");
    }
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public void channelLinked(ChannelUID channelUID) {
    Supplier<CompletableFuture<BacNetClient>> client = () -> getBridgeHandler().flatMap(bridge -> bridge.getClient()).orElse(
      // return failed future if client is not yet ready
      CompletableFuture.completedFuture(null)
    );

    long refreshInterval = Optional.ofNullable(getThing().getChannel(channelUID).getConfiguration())
      .map(cfg -> cfg.as(ChannelConfig.class))
      .map(cfg -> cfg.refreshInterval)
      .filter(interval -> interval != 0)
      .orElseGet(this::getRefreshInterval);

    if (property != null) {
      this.reader = scheduler.scheduleAtFixedRate(new ReadPropertyTask(client, getCallback(), property, channelUID),
        0, refreshInterval, TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public void channelUnlinked(ChannelUID channelUID) {
    if (reader != null) {
      reader.cancel(true);
    }
  }

}
