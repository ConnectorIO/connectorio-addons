package org.connectorio.addons.binding.fatek.internal.channel.converter;

import org.connectorio.addons.binding.fatek.config.channel.binary.DiscreteChannelConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValueDis;

public class DiscreteConverter implements Converter {

  private final DiscreteChannelConfig config;

  public DiscreteConverter(DiscreteChannelConfig config) {
    this.config = config;
  }

  @Override
  public RegValue toValue(Command command) {
    boolean value;
    if (command instanceof OpenClosedType) {
      value = OpenClosedType.CLOSED == command;
    } else {
      value = OnOffType.ON == command;
    }

    if (value) {
      return config.invert ? RegValueDis.FALSE : RegValueDis.TRUE;
    }
    return config.invert ? RegValueDis.TRUE : RegValueDis.FALSE;
  }

  @Override
  public State toState(RegValue value) {
    boolean status = config.invert ? !value.boolValue() : value.boolValue();
    return status ? OnOffType.ON : OnOffType.OFF;
  }
}
