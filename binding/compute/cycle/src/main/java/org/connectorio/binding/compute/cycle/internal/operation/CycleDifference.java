package org.connectorio.binding.compute.cycle.internal.operation;

import java.math.BigDecimal;
import org.connectorio.binding.compute.cycle.internal.CycleOperation;
import org.connectorio.binding.compute.cycle.internal.config.DifferenceChannelConfig;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleDifference implements CycleOperation {

  private final Logger logger = LoggerFactory.getLogger(CycleDifference.class);
  private final ItemRegistry registry;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;
  private final DifferenceChannelConfig config;

  private State initial;

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

  private State readItemState() {
    try {
      Item item = registry.getItem(config.measure);
      State state = item.getStateAs(QuantityType.class);
      if (state != null) {
        return state;
      }
      DecimalType decimalType = item.getStateAs(DecimalType.class);
      if (decimalType != null) {
        return decimalType;
      }
      return DecimalType.ZERO;
    } catch (ItemNotFoundException e) {
      logger.debug("Could not find item {}", config.measure);
    }
    return null;
  }

  @Override
  public void close() {
    State lastValue = readItemState();

    if (initial != null && lastValue != null) {
      State value = null;
      if (lastValue instanceof QuantityType && initial instanceof QuantityType) {
        value = ((QuantityType) lastValue).subtract((QuantityType) initial);
      } else if (lastValue instanceof QuantityType && initial instanceof DecimalType) {
        QuantityType<?> quantity = (QuantityType) lastValue;
        value = new QuantityType<>(quantity.toBigDecimal().subtract(((DecimalType) initial).toBigDecimal()), quantity.getUnit());
      } else if (lastValue instanceof DecimalType && initial instanceof DecimalType) {
        // two decimals
        BigDecimal difference = ((DecimalType) lastValue).toBigDecimal().subtract(((DecimalType) initial).toBigDecimal());
        value = new DecimalType(difference);
      }
      if (value != null) {
        callback.stateUpdated(channelUID, value);
      } else {
        logger.info("Could not determine difference between initial value {} and last value {}", initial, lastValue);
      }
    }
  }

  @Override
  public ChannelUID getChannelId() {
    return channelUID;
  }

}
