package org.connectorio.addons.binding.fatek.internal.channel.converter;

import java.math.BigInteger;
import org.connectorio.addons.binding.fatek.config.channel.data.Data32ChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.data.DataChannelConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValue32;

public class Data16Converter implements Converter {

  private final DataChannelConfig config;

  public Data16Converter(DataChannelConfig config) {
    this.config = config;
  }

  @Override
  public RegValue toValue(Command command) {
    if (!(command instanceof DecimalType)) {
      return null;
    }

    DecimalType decimalType = (DecimalType) command;
    long longValue = decimalType.toBigDecimal().longValue();
    return new RegValue32(config.unsigned ? BigInteger.valueOf(longValue).longValue() : longValue);
  }

  @Override
  public State toState(RegValue value) {
    return new DecimalType(config.unsigned ? value.longValueUnsigned() : value.longValue());
  }
}
