package org.connectorio.binding.compute.cycle.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link CycleBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface CycleBindingConstants {

  String BINDING_ID = "co7io-compute-cycle";

  ThingTypeUID THING_TYPE_CYCLE_COUNTER = new ThingTypeUID(BINDING_ID, "cycle-counter");

  String TIME = "time";
  String COUNT = "count";
  String DIFFERENCE = "difference";

  ChannelTypeUID DIFFERENCE_TYPE = new ChannelTypeUID(BINDING_ID, DIFFERENCE);

}
