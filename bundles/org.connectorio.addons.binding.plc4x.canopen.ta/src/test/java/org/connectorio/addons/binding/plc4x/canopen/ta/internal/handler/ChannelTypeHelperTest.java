package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAADigitalOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAAnalogOutput;
import org.junit.jupiter.api.Test;

class ChannelTypeHelperTest {

  @Test
  void checkAnalogFlowRateOutput() {
    TypeEntry channelType = ChannelTypeHelper.channelType(new TAADigitalOutput((short) 1, 3));

    assertThat(channelType.getChannelType()).matches(type -> type.getId().contains("flow") && type.getId().contains("rate"));
  }

  @Test
  void checkDigitalOutput() {
    TypeEntry channelType = ChannelTypeHelper.channelType(new TAADigitalOutput((short) 0, 43));

    assertThat(channelType.getChannelType()).isEqualTo(TACANopenBindingConstants.DIGITAL_OUTPUT_CHANNEL_TYPE);
  }

  @Test
  void checkTemperatureRegulator() {
    TypeEntry channelType = ChannelTypeHelper.channelType(new TAAnalogOutput((short) 0, 46));

    assertThat(channelType.getChannelType()).matches(type -> type.getId().startsWith(TACANopenBindingConstants.TA_ANALOG_OUTPUT) && type.getId().contains("-temperature"));
  }

}