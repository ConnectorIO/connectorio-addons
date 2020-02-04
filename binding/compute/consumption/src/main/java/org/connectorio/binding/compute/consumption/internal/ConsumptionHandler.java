package org.connectorio.binding.compute.consumption.internal;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class ConsumptionHandler extends BaseThingHandler {

  private final TimeZoneProvider timeZoneProvider;
  private final ItemRegistry itemRegistry;
  private Item item;

  public ConsumptionHandler(Thing thing, TimeZoneProvider timeZoneProvider,
    ItemRegistry itemRegistry) {
    super(thing);
    this.timeZoneProvider = timeZoneProvider;
    this.itemRegistry = itemRegistry;
  }

  @Override
  public void initialize() {
    String item = getConfigAs(ConsumptionConfig.class).item;

    if (item == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, "Missing configuration, please set item");
    } else {
      try {
        Item registryItem = itemRegistry.getItem(item);
        if (registryItem.getType().startsWith(CoreItemFactory.NUMBER)) {
          this.item = registryItem;
          updateStatus(ThingStatus.ONLINE);
        } else {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configured item is not a number");
        }
      } catch (ItemNotFoundException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Item not found");
      }
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  public void channelLinked(ChannelUID channelUID) {
    super.channelLinked(channelUID);

    LocalTime now = LocalTime.now();
    long initialDelay = now.getSecond() == 0 ? 60 : 60 - now.getSecond();

    String computationKind = getChannelId(channelUID);
    switch (computationKind) {
      case ConsumptionBindingConstants.ONE_MINUTE:
        scheduler.scheduleAtFixedRate(new ConsumptionCalculationTask(timeZoneProvider, item, getCallback(), channelUID), initialDelay, 60, TimeUnit.SECONDS);
        break;
    }
  }

  private String getChannelId(ChannelUID channelUID) {
    return channelUID.getId();
  }

  @Override
  public void channelUnlinked(ChannelUID channelUID) {
    super.channelUnlinked(channelUID);
  }

}