package org.connectorio.binding.plc4x.beckhoff.internal;

import org.connectorio.binding.plc4x.shared.Plc4xBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BeckhoffBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface BeckhoffBindingConstants extends Plc4xBindingConstants {

  String BINDING_ID = Plc4xBindingConstants.protocol("ads");

  ThingTypeUID THING_TYPE_NETWORK = new ThingTypeUID(BINDING_ID, "network");

  ThingTypeUID THING_TYPE_SERIAL = new ThingTypeUID(BINDING_ID, "serial");

  ThingTypeUID THING_TYPE_ADS = new ThingTypeUID(BINDING_ID, "ads");

  // List of all Channel types
  String SWITCH = "switch";

  Long DEFAULT_REFRESH_INTERVAL = 1000L;

}
