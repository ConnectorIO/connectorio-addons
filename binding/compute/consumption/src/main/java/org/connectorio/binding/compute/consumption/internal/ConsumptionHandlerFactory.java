package org.connectorio.binding.compute.consumption.internal;

import static org.connectorio.binding.compute.consumption.internal.ConsumptionBindingConstants.THING_TYPE_CONSUMPTION;

import org.connectorio.binding.base.handler.factory.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ConsumptionHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.consumption", service = ThingHandlerFactory.class)
public class ConsumptionHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final TimeZoneProvider timeZoneProvider;
  private final ItemRegistry itemRegistry;

  @Activate
  public ConsumptionHandlerFactory(@Reference TimeZoneProvider timeZoneProvider, @Reference ItemRegistry itemRegistry) {
    super(THING_TYPE_CONSUMPTION);
    this.timeZoneProvider = timeZoneProvider;
    this.itemRegistry = itemRegistry;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_CONSUMPTION.equals(thingTypeUID)) {
      return new ConsumptionHandler(thing, timeZoneProvider, itemRegistry);
    }

    return null;
  }
}
