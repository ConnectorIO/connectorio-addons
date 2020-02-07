package org.connectorio.binding.efficiency.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EfficiencyBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface EfficiencyBindingConstants {

  String BINDING_ID = "co7io-compute-efficiency";

  ThingTypeUID THING_TYPE_VENTILATION_HEAT_EXCHANGER_EFFICIENCY = new ThingTypeUID(BINDING_ID, "ventilation-heat-exchanger-efficiency");

  String EFFICIENCY = "efficiency";

}
