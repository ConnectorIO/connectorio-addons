package org.connectorio.addons.binding.opcua.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.assertj.core.api.ObjectAssert;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.conversions.Conversion;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

class ObjectThingHandlerTest {

  public static final ZoneId UTC = ZoneId.systemDefault();
  static int TYPE_NULL = 0;
  static int TYPE_BOOLEAN = 1;
  static int TYPE_BYTE = 2;
  static int TYPE_UBYTE = 3;
  static int TYPE_SHORT = 4;
  static int TYPE_USHORT = 5;
  static int TYPE_INTEGER = 6;
  static int TYPE_UINTEGER = 7;
  static int TYPE_LONG = 8;
  static int TYPE_ULONG = 9;
  static int TYPE_FLOAT = 10;
  static int TYPE_DOUBLE = 11;
  static int TYPE_STRING = 12;
  static int TYPE_DATETIME = 13;
  static int TYPE_UUID = 14;
  static int TYPE_BYTESTRING = 15;

  // byte
  @MethodSource
  @ParameterizedTest
  void commandConversion(Conversion conversion) {
    verify(map(conversion.command, conversion.channelType), conversion.encodingId)
      .isEqualTo(conversion.value);
  }

  @SuppressWarnings({"unchecked"})
  private <T> ObjectAssert<T> verify(Variant value, Integer typeId) {
    return (ObjectAssert<T>) assertThat(value)
      .isNotNull()
      .matches(e -> Objects.equals(typeId, e.getDataType().map(ExpandedNodeId::getIdentifier)
        .filter(id -> (id instanceof UInteger))
        .map(UInteger.class::cast)
        .map(UInteger::intValue)
        .orElse(null)), String.format("Type id doesn't match %d", typeId))
      .extracting(Variant::getValue);
  }

  private Variant map(Command command, String type) {
    DataValue dataValue = ObjectThingHandler.mapCommand(command, new ChannelTypeUID("co7io-opcua", type));
    if (dataValue == null) {
      return null;
    }
    return dataValue.getValue();
  }

  private static List<Conversion> commandConversion() {
    return Arrays.asList(
      // boolean conversions
      new Conversion("boolean", OnOffType.ON, TYPE_BOOLEAN, true),
      new Conversion("boolean", OnOffType.OFF, TYPE_BOOLEAN, false),
      new Conversion("boolean", OpenClosedType.OPEN, TYPE_BOOLEAN, true),
      new Conversion("boolean", OpenClosedType.CLOSED, TYPE_BOOLEAN, false),
      new Conversion("boolean", new DecimalType(1), TYPE_BOOLEAN, true),
      new Conversion("boolean", new DecimalType(0), TYPE_BOOLEAN, false),
      new Conversion("boolean", new DecimalType(new BigDecimal("0.00000001")), TYPE_BOOLEAN, true),
      new Conversion("boolean", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_BOOLEAN, true),
      new Conversion("boolean", new DecimalType(new BigDecimal(0)), TYPE_BOOLEAN, false),
      new Conversion("boolean", new DecimalType(new BigDecimal(1)), TYPE_BOOLEAN, true),
      new Conversion("boolean", new QuantityType<>(1, Units.KELVIN), TYPE_BOOLEAN, true),
      new Conversion("boolean", new QuantityType<>(0, Units.KELVIN), TYPE_BOOLEAN, false),
      // signed byte conversions
      new Conversion("signed-byte", OnOffType.ON, TYPE_BYTE, (byte) 0x01),
      new Conversion("signed-byte", OnOffType.OFF, TYPE_BYTE, (byte) 0x00),
      new Conversion("signed-byte", OpenClosedType.OPEN, TYPE_BYTE, (byte) 0x01),
      new Conversion("signed-byte", OpenClosedType.CLOSED, TYPE_BYTE, (byte) 0x00),
      new Conversion("signed-byte", new DecimalType(128), TYPE_BYTE, (byte) 0x80),
      new Conversion("signed-byte", new DecimalType(0), TYPE_BYTE, (byte) 0x00),
      new Conversion("signed-byte", new DecimalType(new BigDecimal("0.00000001")), TYPE_BYTE, (byte) 0x00),
      new Conversion("signed-byte", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_BYTE, (byte) 0x00),
      new Conversion("signed-byte", new DecimalType(new BigDecimal(0)), TYPE_BYTE, (byte) 0x00),
      new Conversion("signed-byte", new DecimalType(new BigDecimal(1)), TYPE_BYTE, (byte) 0x01),
      new Conversion("signed-byte", new QuantityType<>(-1, Units.KELVIN), TYPE_BYTE, (byte) 0xFF),
      new Conversion("signed-byte", new QuantityType<>(0, Units.KELVIN), TYPE_BYTE, (byte) 0x00),
      // unsigned byte conversions
      new Conversion("unsigned-byte", OnOffType.ON, TYPE_UBYTE, UByte.valueOf((byte) 0x01)),
      new Conversion("unsigned-byte", OnOffType.OFF, TYPE_UBYTE, UByte.valueOf((byte) 0x00)),
      new Conversion("unsigned-byte", OpenClosedType.OPEN, TYPE_UBYTE, UByte.valueOf((byte) 0x01)),
      new Conversion("unsigned-byte", OpenClosedType.CLOSED, TYPE_UBYTE, UByte.valueOf((byte) 0x00)),
      new Conversion("unsigned-byte", new DecimalType(1), TYPE_UBYTE, UByte.valueOf((byte) 0x01)),
      new Conversion("unsigned-byte", new DecimalType(0), TYPE_UBYTE, UByte.valueOf((byte) 0x00)),
      new Conversion("unsigned-byte", new DecimalType(new BigDecimal("0.00000001")), TYPE_UBYTE, UByte.valueOf((byte) 0x00)),
      new Conversion("unsigned-byte", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_UBYTE, UByte.valueOf((byte) 0x00)),
      new Conversion("unsigned-byte", new DecimalType(new BigDecimal(0)), TYPE_UBYTE, UByte.valueOf((byte) 0x00)),
      new Conversion("unsigned-byte", new DecimalType(new BigDecimal(1)), TYPE_UBYTE, UByte.valueOf((byte) 0x01)),
      new Conversion("unsigned-byte", new QuantityType<>(-1, Units.KELVIN), TYPE_UBYTE, UByte.valueOf((byte) 0xFF)),
      new Conversion("unsigned-byte", new QuantityType<>(0, Units.KELVIN), TYPE_UBYTE, UByte.valueOf((byte) 0x00)),
      // int16
      new Conversion("int16", OnOffType.ON, TYPE_SHORT, ((short) 1)),
      new Conversion("int16", OnOffType.OFF, TYPE_SHORT, ((short) 0)),
      new Conversion("int16", OpenClosedType.OPEN, TYPE_SHORT, ((short) 1)),
      new Conversion("int16", OpenClosedType.CLOSED, TYPE_SHORT, ((short) 0)),
      new Conversion("int16", new DecimalType(1), TYPE_SHORT, ((short) 1)),
      new Conversion("int16", new DecimalType(0), TYPE_SHORT, ((short) 0)),
      new Conversion("int16", new DecimalType(new BigDecimal("0.00000001")), TYPE_SHORT, ((short) 0)),
      new Conversion("int16", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_SHORT, ((short) 0)),
      new Conversion("int16", new DecimalType(new BigDecimal(0)), TYPE_SHORT, ((short) 0)),
      new Conversion("int16", new DecimalType(new BigDecimal(1)), TYPE_SHORT, ((short) 1)),
      new Conversion("int16", new QuantityType<>(-1, Units.KELVIN), TYPE_SHORT, ((short) -1)),
      new Conversion("int16", new QuantityType<>(0, Units.KELVIN), TYPE_SHORT, ((short) 0)),
      // uint16
      new Conversion("uint16", OnOffType.ON, TYPE_USHORT, UShort.valueOf(((short) 1))),
      new Conversion("uint16", OnOffType.OFF, TYPE_USHORT, UShort.valueOf(((short) 0))),
      new Conversion("uint16", OpenClosedType.OPEN, TYPE_USHORT, UShort.valueOf(((short) 1))),
      new Conversion("uint16", OpenClosedType.CLOSED, TYPE_USHORT, UShort.valueOf(((short) 0))),
      new Conversion("uint16", new DecimalType(1), TYPE_USHORT, UShort.valueOf(((short) 1))),
      new Conversion("uint16", new DecimalType(0), TYPE_USHORT, UShort.valueOf(((short) 0))),
      new Conversion("uint16", new DecimalType(new BigDecimal("0.00000001")), TYPE_USHORT, UShort.valueOf(((short) 0))),
      new Conversion("uint16", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_USHORT, UShort.valueOf(((short) 0))),
      new Conversion("uint16", new DecimalType(new BigDecimal(0)), TYPE_USHORT, UShort.valueOf(((short) 0))),
      new Conversion("uint16", new DecimalType(new BigDecimal(1)), TYPE_USHORT, UShort.valueOf(((short) 1))),
      new Conversion("uint16", new QuantityType<>(-1, Units.KELVIN), TYPE_USHORT, UShort.valueOf(((short) 0xFF))),
      new Conversion("uint16", new QuantityType<>(0, Units.KELVIN), TYPE_USHORT, UShort.valueOf(((short) 0))),
      // int32
      new Conversion("int32", OnOffType.ON, TYPE_INTEGER, 1),
      new Conversion("int32", OnOffType.OFF, TYPE_INTEGER, 0),
      new Conversion("int32", OpenClosedType.OPEN, TYPE_INTEGER, 1),
      new Conversion("int32", OpenClosedType.CLOSED, TYPE_INTEGER, 0),
      new Conversion("int32", new DecimalType(1), TYPE_INTEGER, 1),
      new Conversion("int32", new DecimalType(0), TYPE_INTEGER, 0),
      new Conversion("int32", new DecimalType(new BigDecimal("0.00000001")), TYPE_INTEGER, 0),
      new Conversion("int32", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_INTEGER, 0),
      new Conversion("int32", new DecimalType(new BigDecimal(0)), TYPE_INTEGER, 0),
      new Conversion("int32", new DecimalType(new BigDecimal(1)), TYPE_INTEGER, 1),
      new Conversion("int32", new QuantityType<>(-1, Units.KELVIN), TYPE_INTEGER, -1),
      new Conversion("int32", new QuantityType<>(0, Units.KELVIN), TYPE_INTEGER, 0),
      // uint32
      new Conversion("uint32", OnOffType.ON, TYPE_UINTEGER, UInteger.valueOf(((short) 1))),
      new Conversion("uint32", OnOffType.OFF, TYPE_UINTEGER, UInteger.valueOf(((short) 0))),
      new Conversion("uint32", OpenClosedType.OPEN, TYPE_UINTEGER, UInteger.valueOf(((short) 1))),
      new Conversion("uint32", OpenClosedType.CLOSED, TYPE_UINTEGER, UInteger.valueOf(((short) 0))),
      new Conversion("uint32", new DecimalType(1), TYPE_UINTEGER, UInteger.valueOf(((short) 1))),
      new Conversion("uint32", new DecimalType(0), TYPE_UINTEGER, UInteger.valueOf(((short) 0))),
      new Conversion("uint32", new DecimalType(new BigDecimal("0.00000001")), TYPE_UINTEGER, UInteger.valueOf(((short) 0))),
      new Conversion("uint32", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_UINTEGER, UInteger.valueOf(((short) 0))),
      new Conversion("uint32", new DecimalType(new BigDecimal(0)), TYPE_UINTEGER, UInteger.valueOf(((short) 0))),
      new Conversion("uint32", new DecimalType(new BigDecimal(1)), TYPE_UINTEGER, UInteger.valueOf(((short) 1))),
      new Conversion("uint32", new QuantityType<>(-1, Units.KELVIN), TYPE_UINTEGER, UInteger.valueOf(((short) 0xFF))),
      new Conversion("uint32", new QuantityType<>(0, Units.KELVIN), TYPE_UINTEGER, UInteger.valueOf(((short) 0))),
      // int64
      new Conversion("int64", OnOffType.ON, TYPE_LONG, (long) 1),
      new Conversion("int64", OnOffType.OFF, TYPE_LONG, (long) 0),
      new Conversion("int64", OpenClosedType.OPEN, TYPE_LONG, (long) 1),
      new Conversion("int64", OpenClosedType.CLOSED, TYPE_LONG, (long) 0),
      new Conversion("int64", new DecimalType(1), TYPE_LONG, (long) 1),
      new Conversion("int64", new DecimalType(0), TYPE_LONG, (long) 0),
      new Conversion("int64", new DecimalType(new BigDecimal("0.00000001")), TYPE_LONG, (long) 0),
      new Conversion("int64", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_LONG, (long) 0),
      new Conversion("int64", new DecimalType(new BigDecimal(0)), TYPE_LONG, (long) 0),
      new Conversion("int64", new DecimalType(new BigDecimal(1)), TYPE_LONG, (long) 1),
      new Conversion("int64", new QuantityType<>(-1, Units.KELVIN), TYPE_LONG, (long) -1),
      new Conversion("int64", new QuantityType<>(0, Units.KELVIN), TYPE_LONG, (long) 0),
      // uint64
      new Conversion("uint64", OnOffType.ON, TYPE_ULONG, ULong.valueOf(1)),
      new Conversion("uint64", OnOffType.OFF, TYPE_ULONG, ULong.valueOf(0)),
      new Conversion("uint64", OpenClosedType.OPEN, TYPE_ULONG, ULong.valueOf(1)),
      new Conversion("uint64", OpenClosedType.CLOSED, TYPE_ULONG, ULong.valueOf(0)),
      new Conversion("uint64", new DecimalType(1), TYPE_ULONG, ULong.valueOf(1)),
      new Conversion("uint64", new DecimalType(0), TYPE_ULONG, ULong.valueOf(0)),
      new Conversion("uint64", new DecimalType(new BigDecimal("0.00000001")), TYPE_ULONG, ULong.valueOf(0)),
      new Conversion("uint64", new DecimalType(new BigDecimal("0.000000000000001")), TYPE_ULONG, ULong.valueOf(0)),
      new Conversion("uint64", new DecimalType(new BigDecimal(0)), TYPE_ULONG, ULong.valueOf(0)),
      new Conversion("uint64", new DecimalType(new BigDecimal(1)), TYPE_ULONG, ULong.valueOf(1)),
      new Conversion("uint64", new QuantityType<>(-1, Units.KELVIN), TYPE_ULONG, ULong.valueOf(-1)),
      new Conversion("uint64", new QuantityType<>(0, Units.KELVIN), TYPE_ULONG, ULong.valueOf(0)),
      // float
      new Conversion("float", OnOffType.ON, TYPE_FLOAT, (float) 1),
      new Conversion("float", OnOffType.OFF, TYPE_FLOAT, (float) 0),
      new Conversion("float", OpenClosedType.OPEN, TYPE_FLOAT, (float) 1),
      new Conversion("float", OpenClosedType.CLOSED, TYPE_FLOAT, (float) 0),
      new Conversion("float", new DecimalType(1), TYPE_FLOAT, (float) 1),
      new Conversion("float", new DecimalType(0), TYPE_FLOAT, (float) 0),
      new Conversion("float", new DecimalType(new BigDecimal("0.01")), TYPE_FLOAT, (float) 0.01),
      new Conversion("float", new DecimalType(new BigDecimal("0.001")), TYPE_FLOAT, (float) 0.001),
      new Conversion("float", new DecimalType(new BigDecimal(0)), TYPE_FLOAT, (float) 0),
      new Conversion("float", new DecimalType(new BigDecimal(1)), TYPE_FLOAT, (float) 1),
      new Conversion("float", new QuantityType<>(-1, Units.KELVIN), TYPE_FLOAT, (float) -1),
      new Conversion("float", new QuantityType<>(0, Units.KELVIN), TYPE_FLOAT, (float) 0),
      // double
      new Conversion("double", OnOffType.ON, TYPE_DOUBLE, (double) 1),
      new Conversion("double", OnOffType.OFF, TYPE_DOUBLE, (double) 0),
      new Conversion("double", OpenClosedType.OPEN, TYPE_DOUBLE, (double) 1),
      new Conversion("double", OpenClosedType.CLOSED, TYPE_DOUBLE, (double) 0),
      new Conversion("double", new DecimalType(1), TYPE_DOUBLE, (double) 1),
      new Conversion("double", new DecimalType(0), TYPE_DOUBLE, (double) 0),
      new Conversion("double", new DecimalType(new BigDecimal("0.01")), TYPE_DOUBLE, (double) 0.01),
      new Conversion("double", new DecimalType(new BigDecimal("0.001")), TYPE_DOUBLE, (double) 0.001),
      new Conversion("double", new DecimalType(new BigDecimal(0)), TYPE_DOUBLE, (double) 0),
      new Conversion("double", new DecimalType(new BigDecimal(1)), TYPE_DOUBLE, (double) 1),
      new Conversion("double", new QuantityType<>(-1, Units.KELVIN), TYPE_DOUBLE, (double) -1),
      new Conversion("double", new QuantityType<>(0, Units.KELVIN), TYPE_DOUBLE, (double) 0),
      // string
      new Conversion("string", new StringType("ab"), TYPE_STRING, "ab"),
      new Conversion("string", new StringType(""), TYPE_STRING, ""),
      new Conversion("string", OnOffType.ON, TYPE_STRING, "ON"),
      new Conversion("string", OnOffType.OFF, TYPE_STRING, "OFF"),
      new Conversion("string", OpenClosedType.OPEN, TYPE_STRING, "OPEN"),
      new Conversion("string", OpenClosedType.CLOSED, TYPE_STRING, "CLOSED"),
      new Conversion("string", new DecimalType(1), TYPE_STRING, "1"),
      new Conversion("string", new DecimalType(0), TYPE_STRING, "0"),
      new Conversion("string", new DecimalType(new BigDecimal("0.01")), TYPE_STRING, "0.01"),
      new Conversion("string", new DecimalType(new BigDecimal("0.001")), TYPE_STRING, "0.001"),
      new Conversion("string", new DecimalType(new BigDecimal(0)), TYPE_STRING, "0"),
      new Conversion("string", new DecimalType(new BigDecimal(1)), TYPE_STRING, "1"),
      new Conversion("string", new QuantityType<>(-1, Units.KELVIN), TYPE_STRING, "-1 K"),
      new Conversion("string", new QuantityType<>(0, Units.KELVIN), TYPE_STRING, "0 K"),
      // date time
      new Conversion("datetime", OnOffType.ON, null, null),
      new Conversion("datetime", OnOffType.OFF, null, null),
      new Conversion("datetime", new DateTimeType(
        zonedInstant(1, 2, 3, 4, 5, 6, 700)), TYPE_DATETIME,
        new DateTime(instant(1, 2, 3, 4, 5, 6, 700))
      ),
      new Conversion("datetime", OpenClosedType.OPEN, null, null),
      new Conversion("datetime", OpenClosedType.CLOSED, null, null),
      new Conversion("datetime", new DecimalType(1), TYPE_DATETIME,
        new DateTime(zonedInstant(1970, 1, 1, 0, 0, 1, 0).toInstant())
      ),
      new Conversion("datetime", new DecimalType(-1), TYPE_DATETIME,
        new DateTime(zonedInstant(1969, 12, 31, 23, 59, 59, 0).toInstant())
      ),
      new Conversion("datetime", new DecimalType(0), TYPE_DATETIME,
        new DateTime(zonedInstant(1970, 1, 1, 0, 0, 0, 0).toInstant())
      ),
      new Conversion("datetime", new DecimalType(new BigDecimal("0.01")), TYPE_DATETIME,
        new DateTime(zonedInstant(1970, 1, 1, 0, 0, 0, 0).toInstant())
      ),
      new Conversion("datetime", new DecimalType(new BigDecimal("0.001")), TYPE_DATETIME,
        new DateTime(zonedInstant(1970, 1, 1, 0, 0, 0, 0).toInstant())
      ),
      new Conversion("datetime", new DecimalType(new BigDecimal(0)), TYPE_DATETIME,
        new DateTime(zonedInstant(1970, 1, 1, 0, 0, 0, 0).toInstant())
      ),
      new Conversion("datetime", new DecimalType(new BigDecimal(1)), TYPE_DATETIME,
        new DateTime(zonedInstant(1970, 1, 1, 0, 0, 1, 0).toInstant())
      ),
      new Conversion("datetime", new QuantityType<>(-1, Units.KELVIN), null, null),
      new Conversion("datetime", new QuantityType<>(0, Units.KELVIN), null, null)
    );
  }

  static class Conversion {
    final String channelType;
    final Command command;
    final Integer encodingId;
    final Object value;

    Conversion(String channelType, Command command, Integer encodingId, Object value) {
      this.channelType = channelType;
      this.command = command;
      this.encodingId = encodingId;
      this.value = value;
    }

    @Override
    public String toString() {
      return "ch=" + channelType + ": "
          + "cmd=" + command + "(" + command.getClass().getSimpleName() + ")->"
          + value + " (id=" + encodingId + "; " + (value == null ? null : value.getClass().getSimpleName()) + ")";
    }
  }

  private static ZonedDateTime zonedInstant(int year, int month, int day, int hour, int minute, int second, int nanos) {
    return ZonedDateTime.ofInstant(instant(year, month, day, hour, minute, second, nanos), ZoneOffset.UTC);
  }

  private static Instant instant(int year, int month, int day, int hour, int minute, int second, int nanos) {
    return LocalDateTime.of(year, month, day, hour, minute, second, nanos).atOffset(ZoneOffset.UTC).toInstant();
  }

}