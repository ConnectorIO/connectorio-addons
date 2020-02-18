package org.connectorio.binding.efficiency.internal;

import static org.connectorio.binding.efficiency.internal.EfficiencyBindingConstants.THING_TYPE_VENTILATION_HEAT_EXCHANGER;

import org.connectorio.binding.base.handler.factory.BaseThingHandlerFactory;
import org.connectorio.binding.efficiency.internal.memo.StateCollector;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EfficiencyHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.efficiency", service = ThingHandlerFactory.class)
public class EfficiencyHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final ItemRegistry itemRegistry;
  private final StateCollector<ItemStateChangedEvent> collector;

  @Activate
  public EfficiencyHandlerFactory(@Reference ItemRegistry itemRegistry, @Reference StateCollector<ItemStateChangedEvent> collector) {
    super(THING_TYPE_VENTILATION_HEAT_EXCHANGER);
    this.itemRegistry = itemRegistry;
    this.collector = collector;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_VENTILATION_HEAT_EXCHANGER.equals(thingTypeUID)) {
      return new EfficiencyHandler(thing, itemRegistry, collector);
    }

    return null;
  }
}
