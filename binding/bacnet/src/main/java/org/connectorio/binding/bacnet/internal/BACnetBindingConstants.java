package org.connectorio.binding.bacnet.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public interface BACnetBindingConstants {

    String BINDING_ID = "co7io-bacnet";

    // bridge types
    String IPV4_BRIDGE_TYPE = "ipv4";
    String MSTP_BRIDGE_TYPE = "mstp";
    String DEVICE_TYPE = "device"; // it is a bridge to property objects

    // property kinds
    String ANALOG_INPUT_TYPE = "analog-input";
    String ANALOG_OUTPUT_TYPE = "analog-output";
    String ANALOG_VALUE_TYPE = "analog-value";

    String BINARY_INPUT_TYPE = "binary-input";
    String BINARY_OUTPUT_TYPE = "binary-output";
    String BINARY_VALUE_TYPE = "binary-value";

    String CALENDAR_TYPE = "calendar";

    String MULTISTATE_INPUT_TYPE = "multistate-input";
    String MULTISTATE_OUTPUT_TYPE = "multistate-output";
    String MULTISTATE_VALUE_TYPE = "multistate-value";

    String PULSE_CONVERTER_TYPE = "pulse-converter";
    String SCHEDULE_TYPE = "schedule";

    String CHARACTER_STRING_TYPE = "character-string";

    String DATE_TIME_TYPE = "date-time";
    String LARGE_ANALOG_TYPE = "large-analog";
    String OCTET_STRING_TYPE = "octet-string";

    String TIME_TYPE = "time";
    String INTEGER_TYPE = "integer";
    String POSITIVE_INTEGER_TYPE = "postitive-integer";

    String DATE_TIME_PATTERN_TYPE = "date-time-pattern";
    String TIME_PATTERN_TYPE = "time-pattern";
    String DATE_PATTERN_TYPE = "date-pattern";
    String ACCUMULATOR_TYPE = "accumulator";

    ThingTypeUID IPV4_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, IPV4_BRIDGE_TYPE);
    ThingTypeUID MSTP_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, MSTP_BRIDGE_TYPE);
    ThingTypeUID DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_TYPE);

    ThingTypeUID ANALOG_INPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, ANALOG_INPUT_TYPE);
    ThingTypeUID ANALOG_OUTPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, ANALOG_OUTPUT_TYPE);
    ThingTypeUID ANALOG_VALUE_THING_TYPE = new ThingTypeUID(BINDING_ID, ANALOG_VALUE_TYPE);

    ThingTypeUID BINARY_INPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, BINARY_INPUT_TYPE);
    ThingTypeUID BINARY_OUTPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, BINARY_OUTPUT_TYPE);
    ThingTypeUID BINARY_VALUE_THING_TYPE = new ThingTypeUID(BINDING_ID, BINARY_VALUE_TYPE);

    ThingTypeUID CALENDAR_THING_TYPE = new ThingTypeUID(BINDING_ID, CALENDAR_TYPE);

    ThingTypeUID MULTISTATE_INPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, MULTISTATE_INPUT_TYPE);
    ThingTypeUID MULTISTATE_OUTPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, MULTISTATE_OUTPUT_TYPE);
    ThingTypeUID MULTISTATE_VALUE_THING_TYPE = new ThingTypeUID(BINDING_ID, MULTISTATE_VALUE_TYPE);

    ThingTypeUID PULSE_CONVETHING_RTER = new ThingTypeUID(BINDING_ID, PULSE_CONVERTER_TYPE);
    ThingTypeUID SCHEDULE_THING_TYPE = new ThingTypeUID(BINDING_ID, SCHEDULE_TYPE);

    ThingTypeUID CHARACTER_STRING_THING_TYPE = new ThingTypeUID(BINDING_ID, CHARACTER_STRING_TYPE);

    ThingTypeUID DATE_TIME_THING_TYPE = new ThingTypeUID(BINDING_ID, DATE_TIME_TYPE);
    ThingTypeUID LARGE_ANALOG_THING_TYPE = new ThingTypeUID(BINDING_ID, LARGE_ANALOG_TYPE);
    ThingTypeUID OCTET_STRING_THING_TYPE = new ThingTypeUID(BINDING_ID, OCTET_STRING_TYPE);

    ThingTypeUID TIME_THING_TYPE = new ThingTypeUID(BINDING_ID, TIME_TYPE);
    ThingTypeUID INTEGER_THING_TYPE = new ThingTypeUID(BINDING_ID, INTEGER_TYPE);
    ThingTypeUID POSITIVE_INTEGER_THING_TYPE = new ThingTypeUID(BINDING_ID, POSITIVE_INTEGER_TYPE);

    ThingTypeUID DATE_TIME_PATTERN_THING_TYPE = new ThingTypeUID(BINDING_ID, DATE_TIME_PATTERN_TYPE);
    ThingTypeUID TIME_PATTERN_THING_TYPE = new ThingTypeUID(BINDING_ID, TIME_PATTERN_TYPE);
    ThingTypeUID DATE_PATTERN_THING_TYPE = new ThingTypeUID(BINDING_ID, DATE_PATTERN_TYPE);
    ThingTypeUID ACCUMULATOR_THING_TYPE = new ThingTypeUID(BINDING_ID, ACCUMULATOR_TYPE);

    Long DEFAULT_POLLING_INTERVAL = 1000L;
}