package org.connectorio.binding.compute.cycle.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.connectorio.binding.compute.cycle.internal.CycleBindingConstants.*;
import static org.mockito.Mockito.*;

import org.connectorio.binding.compute.cycle.internal.config.CycleCounterConfig;
import org.connectorio.binding.compute.cycle.internal.config.DifferenceChannelConfig;
import org.connectorio.binding.compute.cycle.internal.operation.CycleDifference;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DifferenceTest {

  final static String TRIGGER = "valve-open";
  final static String MEASURE = "gas-meter";

  public static ThingUID THING_UID = new ThingUID(THING_TYPE_CYCLE_COUNTER, "cnt-x");
  public static final ChannelUID DIFFERENCE_CHANNEL = new ChannelUID(THING_UID, "difference-1");

  @Mock
  ThingHandlerCallback callback;

  @Mock
  ItemRegistry registry;

  CycleCounterConfig config = new CycleCounterConfig() {{
    trigger = TRIGGER;
  }};

  DifferenceChannelConfig channelConfig = new DifferenceChannelConfig() {{
    measure = MEASURE;
  }};

  @Test
  void testBasicCycle() {
    Item item = mock(Item.class);
    when(item.getStateAs(QuantityType.class)).thenReturn(QuantityType.valueOf(100.0, SmartHomeUnits.WATT_HOUR))
      .thenReturn(QuantityType.valueOf(200.01, SmartHomeUnits.WATT_HOUR));

    try {
      when(registry.getItem(MEASURE)).thenReturn(item);
    } catch (ItemNotFoundException e) {
      // not relevant
    }

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleDifference(registry, callback, DIFFERENCE_CHANNEL, channelConfig));
    receiver.accept(event(TRIGGER, OnOffType.ON));
    receiver.accept(event(TRIGGER, OnOffType.OFF));

    verify(callback).stateUpdated(DIFFERENCE_CHANNEL, QuantityType.valueOf(100.01, SmartHomeUnits.WATT_HOUR));
  }

  private static ItemStateChangedEvent event(String itemName, State newState) {
    return event(itemName, newState, UnDefType.NULL);
  }

  private static ItemStateChangedEvent event(String itemName, State newState, UnDefType oldState) {
    ItemStateChangedEvent event = Mockito.mock(ItemStateChangedEvent.class, withSettings().lenient());
    when(event.getItemName()).thenReturn(itemName);
    when(event.getItemState()).thenReturn(newState);
    when(event.getOldItemState()).thenReturn(oldState);
    return event;
  }

}