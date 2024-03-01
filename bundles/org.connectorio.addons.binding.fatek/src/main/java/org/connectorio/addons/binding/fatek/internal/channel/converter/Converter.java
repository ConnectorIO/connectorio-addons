package org.connectorio.addons.binding.fatek.internal.channel.converter;

import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.RegValue;

public interface Converter {

  RegValue toValue(Command command);

  State toState(RegValue value);

}
