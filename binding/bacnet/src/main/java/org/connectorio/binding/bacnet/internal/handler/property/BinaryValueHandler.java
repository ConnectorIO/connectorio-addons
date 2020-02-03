package org.connectorio.binding.bacnet.internal.handler.property;

import com.serotonin.bacnet4j.obj.AnalogInputObject;
import org.code_house.bacnet4j.wrapper.api.Type;
import org.connectorio.binding.bacnet.internal.config.ObjectConfig;
import org.eclipse.smarthome.core.thing.Thing;

public class BinaryValueHandler extends BACnetPropertyHandler<AnalogInputObject, BACnetDeviceBridgeHandler<?, ?>, ObjectConfig> {

  /**
   * Creates a new instance of this class for the {@link Thing}.
   *
   * @param thing the thing that should be handled, not null
   */
  public BinaryValueHandler(Thing thing) {
    super(thing, Type.BINARY_VALUE);
  }


}
