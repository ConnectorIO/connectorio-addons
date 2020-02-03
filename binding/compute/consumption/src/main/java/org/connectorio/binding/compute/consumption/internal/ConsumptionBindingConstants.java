package org.connectorio.binding.compute.consumption.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ConsumptionBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface ConsumptionBindingConstants {

  String BINDING_ID = "co7io-compute-consumption";

  ThingTypeUID THING_TYPE_CONSUMPTION = new ThingTypeUID(BINDING_ID, "consumption");

  // List of all Channel kinds computed by handler (s)
  String ONE_MINUTE = "oneMinute";
  String FIVE_MINUTES = "fiveMinutes";
  String FIFTEEN_MINUTES = "fifteenMinutes";
  String THIRTY_MINUTES = "thirtyMinutes";
  String HOURLY = "hourly";
  String DAILY = "daily";
  String MONTHLY = "monthly";

}
