package org.connectorio.binding.efficiency.internal.ventilation.heatex;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.connectorio.binding.efficiency.internal.memo.StateReceiver;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;

/**
 * A very basic implementation of naive calculation of heat efficiency algorithm.
 * It waits for updates from given items
 */
public class HeatExState implements StateReceiver<ItemStateChangedEvent> {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
  private final Supplier<Long> timeSupplier;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;
  private final HeatExConfig config;

  private Entry<Long, BigDecimal> intakeTemperature;
  private Entry<Long, BigDecimal> supplyTemperature;
  private Entry<Long, BigDecimal> extractTemperature;

  public HeatExState(Supplier<Long> timeSupplier, ThingHandlerCallback callback, ChannelUID channelUID, HeatExConfig config) {
    this.timeSupplier = timeSupplier;
    this.callback = callback;
    this.channelUID = channelUID;
    this.config = config;
  }

  @Override
  public void accept(ItemStateChangedEvent event) {
    long currentTime = timeSupplier.get();
    if (config.extractTemperature.equals(event.getItemName())) {
      extractTemperature = extract(currentTime, event.getItemState());
    }
    if (config.supplyTemperature.equals(event.getItemName())) {
      supplyTemperature = extract(currentTime, event.getItemState());
    }
    if (config.intakeTemperature.equals(event.getItemName())) {
      intakeTemperature = extract(currentTime, event.getItemState());
    }

    if (isExpired(currentTime, intakeTemperature) || isExpired(currentTime, supplyTemperature) || isExpired(currentTime, extractTemperature)) {
      reset();
      return;
    }

    if (intakeTemperature != null && supplyTemperature != null && extractTemperature != null) {
      BigDecimal intake = intakeTemperature.getValue();
      BigDecimal supplyPart = supplyTemperature.getValue().subtract(intake, MATH_CONTEXT);
      BigDecimal extractPart = extractTemperature.getValue().subtract(intake, MATH_CONTEXT);

      if (extractPart.compareTo(BigDecimal.ZERO) != 0) {
        BigDecimal efficiency = supplyPart.divide(extractPart, MATH_CONTEXT);
        DecimalType percentage = new PercentType(efficiency.multiply(BigDecimal.valueOf(100), MATH_CONTEXT));
        callback.stateUpdated(channelUID, percentage);
      } else {
        callback.stateUpdated(channelUID, PercentType.ZERO);
      }
      reset();
    }

  }

  private void reset() {
    intakeTemperature = supplyTemperature = extractTemperature = null;
  }

  private boolean isExpired(long currentTime, Entry<Long, BigDecimal> entry) {
    return entry != null && entry.getKey() < currentTime - TimeUnit.SECONDS.toMillis(config.cycleTime);
  }

  private Entry<Long, BigDecimal> extract(long currentTime, State state) {
    if (state == null) {
      return null;
    }

    return Map.entry(currentTime, state.as(DecimalType.class).toBigDecimal());
  }

}
