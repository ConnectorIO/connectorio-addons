package org.connectorio.binding.compute.cycle.internal.operation;

import java.math.BigDecimal;
import org.connectorio.binding.compute.cycle.internal.CycleOperation;
import org.connectorio.binding.compute.cycle.internal.config.DifferenceChannelConfig;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleDifference implements CycleOperation /*, StateReceiver<ItemStateChangedEvent>*/ {

  private final Logger logger = LoggerFactory.getLogger(CycleDifference.class);
  private final ItemRegistry registry;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;
  private final DifferenceChannelConfig config;

  private BigDecimal initial;

  public CycleDifference(ItemRegistry registry, ThingHandlerCallback callback, ChannelUID channelUID, DifferenceChannelConfig config) {
    this.registry = registry;
    this.callback = callback;
    this.channelUID = channelUID;
    this.config = config;
  }

  @Override
  public void open() {
    this.initial = readItemState();
  }

  private BigDecimal readItemState() {
    try {
      Item item = registry.getItem(config.measure);
      return item.getStateAs(DecimalType.class).toBigDecimal();
    } catch (ItemNotFoundException e) {
      logger.debug("Could not find item {}", config.measure);
    }
    return null;
  }

  @Override
  public void close() {
    BigDecimal lastValue = readItemState();

    if (initial != null && lastValue != null) {
      callback.stateUpdated(channelUID, new DecimalType(lastValue.subtract(initial)));
    }
  }

  /*
  @Override
  public void accept(ItemStateChangedEvent itemStateChangedEvent) {
    if (config.measure.equals(itemStateChangedEvent.getItemName())) {
      if (initial == null) {
        initial = itemStateChangedEvent.getItemState().as(DecimalType.class).toBigDecimal();
      } else {
        last = itemStateChangedEvent.getItemState().as(DecimalType.class).toBigDecimal();
      }
    }
  }
  */
}
