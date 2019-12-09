package org.connectorio.binding.base.handler.factory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * A very basic top class for binding implementations which contains the most common part of all
 * bindings.
 *
 * @author Łukasz Dywicki
 */
public abstract class BaseThingHandlerFactory extends org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory {

  private final Set<ThingTypeUID> supportedThings;

  protected BaseThingHandlerFactory(ThingTypeUID ... supportedThings) {
    this(new HashSet<>(Arrays.asList(supportedThings)));
  }

  protected BaseThingHandlerFactory(Set<ThingTypeUID> supportedThings) {
    this.supportedThings = supportedThings;
  }

  @Override
  public boolean supportsThingType(ThingTypeUID thingTypeUID) {
    return supportedThings.contains(thingTypeUID);
  }

}
