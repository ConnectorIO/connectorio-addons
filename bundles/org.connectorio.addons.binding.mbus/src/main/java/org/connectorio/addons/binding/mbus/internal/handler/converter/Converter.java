package org.connectorio.addons.binding.mbus.internal.handler.converter;

import org.openhab.core.types.State;
import org.openmuc.jmbus.DataRecord;

public interface Converter {

  State toState(DataRecord record);

}
