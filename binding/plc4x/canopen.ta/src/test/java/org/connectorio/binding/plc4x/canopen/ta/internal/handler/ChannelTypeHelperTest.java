package org.connectorio.binding.plc4x.canopen.ta.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.connectorio.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAADigitalOutput;
import org.junit.jupiter.api.Test;

class ChannelTypeHelperTest {

  @Test
  void checkDigitalOutput() {
    TypeEntry channelType = ChannelTypeHelper.channelType(new TAADigitalOutput((short) 0, 43));

    assertThat(channelType.getChannelType()).isEqualTo(TACANopenBindingConstants.DIGITAL_OUTPUT_CHANNEL_TYPE);
  }

}