package org.connectorio.binding.compute.consumption.internal;

import java.time.ZonedDateTime;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;

public class ConsumptionCalculationTask implements Runnable {

  private final TimeZoneProvider timeZoneProvider;
  private final Item item;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;

  public ConsumptionCalculationTask(TimeZoneProvider timeZoneProvider, Item item, ThingHandlerCallback callback, ChannelUID channelUID) {
    this.timeZoneProvider = timeZoneProvider;
    this.item = item;
    this.callback = callback;
    this.channelUID = channelUID;
  }

  @Override
  public void run() {
    ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone()).minusSeconds(60);

    DecimalType deltaSince = PersistenceExtensions.deltaSince(item, now.toInstant());
    if (deltaSince != null) {
      callback.stateUpdated(channelUID, deltaSince);
    }
  }
}
