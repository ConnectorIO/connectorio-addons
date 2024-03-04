package org.connectorio.addons.binding.plc4x.source;

import org.openhab.core.types.State;

public interface Converter {

  State fromValue(Object value);

}
