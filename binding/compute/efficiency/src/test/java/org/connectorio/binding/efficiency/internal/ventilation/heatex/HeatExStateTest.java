package org.connectorio.binding.efficiency.internal.ventilation.heatex;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.function.Supplier;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeatExStateTest {

  final static String INTAKE = "intake";
  final static String SUPPLY = "supply";
  final static String EXTRACT = "extract";

  @Mock
  ThingHandlerCallback callback;

  @Mock
  ChannelUID channelUID;

  HeatExConfig config = new HeatExConfig() {{
    intakeTemperature = INTAKE;
    supplyTemperature = SUPPLY;
    extractTemperature = EXTRACT;
  }};

  @Test
  void testZeroEfficiency() {
    Supplier<Long> time = System::currentTimeMillis;

    HeatExState heatExState = new HeatExState(time, callback, channelUID, config);
    heatExState.accept(event(INTAKE, DecimalType.valueOf("50.0")));
    heatExState.accept(event(SUPPLY, DecimalType.valueOf("50.0")));
    heatExState.accept(event(EXTRACT, DecimalType.valueOf("50.0")));

    verify(callback).stateUpdated(channelUID, PercentType.ZERO);
  }

  @Test
  void testNormalEfficiency() {
    Supplier<Long> time = System::currentTimeMillis;

    HeatExState heatExState = new HeatExState(time, callback, channelUID, config);
    heatExState.accept(event(INTAKE, DecimalType.valueOf("10.0")));
    heatExState.accept(event(SUPPLY, DecimalType.valueOf("20.0")));
    heatExState.accept(event(EXTRACT, DecimalType.valueOf("30.0")));

    verify(callback).stateUpdated(channelUID, PercentType.valueOf("50"));
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