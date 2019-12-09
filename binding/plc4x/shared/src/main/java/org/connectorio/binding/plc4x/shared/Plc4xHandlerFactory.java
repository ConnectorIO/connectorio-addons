package org.connectorio.binding.plc4x.shared;

import java.util.Set;
import org.connectorio.binding.base.handler.factory.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Implementation of base class for PLC4X specific handler factories.
 */
public abstract class Plc4xHandlerFactory extends BaseThingHandlerFactory {

  public Plc4xHandlerFactory(ThingTypeUID ... supportedThings) {
    super(supportedThings);
  }

  public Plc4xHandlerFactory(Set<ThingTypeUID> supportedThings) {
    super(supportedThings);
  }
}
