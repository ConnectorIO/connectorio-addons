package org.connectorio.binding.plc4x.canopen.internal;


import org.eclipse.smarthome.core.thing.ThingTypeUID;

public interface CANopenBindingConstants extends org.connectorio.binding.plc4x.canopen.CANopenBindingConstants {

  // bridge types
  String SOCKETCAN_BRIDGE_TYPE = "socketcan";

  String GENERIC_THING = "generic";
  String SDO_THING = "sdo";

  ThingTypeUID SOCKETCAN_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, SOCKETCAN_BRIDGE_TYPE);

  ThingTypeUID GENERIC_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, GENERIC_THING);

  ThingTypeUID SDO_THING_TYPE = new ThingTypeUID(BINDING_ID, SDO_THING);

}
