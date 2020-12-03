package org.connectorio.binding.plc4x.canopen.ta.internal.handler;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.connectorio.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

@ExtendWith(MockitoExtension.class)
class ThingChannelValueListenerTest {

  ThingChannelValueListener listener;

  @Mock
  ThingHandlerCallback callback;
  @Mock
  Thing thing;
  @Mock
  Function<TAValue, State> converter;

  @Mock
  Channel channel;

  @BeforeEach
  void setup() {
    listener = new ThingChannelValueListener(callback, thing, converter);
  }

  @Test
  void verifyTaUnitConversion() {
  }

  @Test
  void checkAnalogOutput() throws ParseException {
    WriteBuffer writeBuffer = new WriteBuffer(2, true);
    writeBuffer.writeInt(16, 459);
    ReadBuffer buffer = new ReadBuffer(writeBuffer.getData(), true);

    Map<String, Object> config = Collections.singletonMap("unit", 2);
    when(thing.getChannel("analog#" + 0)).thenReturn(channel);
    when(channel.getConfiguration()).thenReturn(new Configuration(config));

    listener.analog(0, buffer);
  }

}