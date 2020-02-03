package org.connectorio.binding.bacnet.internal;

import org.connectorio.binding.bacnet.internal.handler.property.AnalogOutputHandler;
import org.connectorio.binding.bacnet.internal.handler.property.AnalogValueHandler;
import org.connectorio.binding.bacnet.internal.handler.property.BACnetDeviceHandler;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetIpv4BridgeHandler;
import org.connectorio.binding.bacnet.internal.handler.property.AnalogInputHandler;
import org.connectorio.binding.bacnet.internal.handler.property.BinaryInputHandler;
import org.connectorio.binding.bacnet.internal.handler.property.BinaryOutputHandler;
import org.connectorio.binding.bacnet.internal.handler.property.BinaryValueHandler;
import org.connectorio.binding.bacnet.internal.handler.property.MultiStateInputHandler;
import org.connectorio.binding.bacnet.internal.handler.property.MultiStateOutputHandler;
import org.connectorio.binding.bacnet.internal.handler.property.MultiStateValueHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {BACnetThingHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class})
public class BACnetThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory, BACnetBindingConstants {

  private final Logger logger = LoggerFactory.getLogger(BACnetThingHandlerFactory.class);

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (thing instanceof Bridge) {
      if (DEVICE_THING_TYPE.equals(thingTypeUID)) {
        return new BACnetDeviceHandler((Bridge) thing);
      } else if (IPV4_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
        return new BACnetIpv4BridgeHandler((Bridge) thing);
//      } else if (IPV6_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
//        return new BACnetIpv4BridgeHandler(bundleContext, (Bridge) thing);
      } else if (MSTP_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
        logger.warn("MSTP networks are not supported yet");
        return null;
      }
    }

    if (ANALOG_INPUT_THING_TYPE.equals(thingTypeUID)) {
      return new AnalogInputHandler(thing);
    } else if (ANALOG_OUTPUT_THING_TYPE.equals(thingTypeUID)) {
      return new AnalogOutputHandler(thing);
    } else if (ANALOG_VALUE_THING_TYPE.equals(thingTypeUID)) {
      return new AnalogValueHandler(thing);
    } else if (BINARY_INPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BinaryInputHandler(thing);
    } else if (BINARY_OUTPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BinaryOutputHandler(thing);
    } else if (BINARY_VALUE_THING_TYPE.equals(thingTypeUID)) {
      return new BinaryValueHandler(thing);
    } else if (MULTISTATE_INPUT_THING_TYPE.equals(thingTypeUID)) {
      return new MultiStateInputHandler(thing);
    } else if (MULTISTATE_OUTPUT_THING_TYPE.equals(thingTypeUID)) {
      return new MultiStateOutputHandler(thing);
    } else if (MULTISTATE_VALUE_THING_TYPE.equals(thingTypeUID)) {
      return new MultiStateValueHandler(thing);
    }

    return null;
  }

  @Override
  public boolean supportsThingType(ThingTypeUID thingTypeUID) {
    return BINDING_ID.equals(thingTypeUID.getBindingId());
  }
}
