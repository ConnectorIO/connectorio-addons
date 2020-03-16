package org.connectorio.binding.compute.cycle.internal;

import static org.connectorio.binding.compute.cycle.internal.CycleBindingConstants.THING_TYPE_CYCLE_COUNTER;

import org.connectorio.binding.base.handler.factory.BaseThingHandlerFactory;
import org.connectorio.binding.compute.cycle.internal.memo.StateCollector;
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
 * The {@link CycleHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.cycle", service = ThingHandlerFactory.class)
public class CycleHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final ItemRegistry itemRegistry;
  private final StateCollector<ItemStateChangedEvent> collector;

  @Activate
  public CycleHandlerFactory(@Reference ItemRegistry itemRegistry, @Reference StateCollector<ItemStateChangedEvent> collector) {
    super(THING_TYPE_CYCLE_COUNTER);
    this.itemRegistry = itemRegistry;
    this.collector = collector;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_CYCLE_COUNTER.equals(thingTypeUID)) {
      return new CycleCounterHandler(thing, itemRegistry, collector);
    }

    return null;
  }
}
