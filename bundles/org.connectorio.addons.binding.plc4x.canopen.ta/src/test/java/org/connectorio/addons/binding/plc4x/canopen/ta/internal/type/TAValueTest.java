package org.connectorio.addons.binding.plc4x.canopen.ta.internal.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.List;
import javax.measure.Quantity;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ComplexUnit;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import tec.uom.se.quantity.Quantities;

class TAValueTest {

  @Test
  void checkUnitConversion() {
    TAValue value = new TAValue(AnalogUnit.BAR.getIndex(), 100);

    assertThat(value.getValue()).isInstanceOf(Quantity.class)
      .isEqualTo(Quantities.getQuantity(1.0, Units.BAR));

    Quantity<?> quantity = (Quantity<?>) value.getValue();
    QuantityType<?> quantityType = new QuantityType<>(quantity.getValue(), quantity.getUnit());

    assertThat(quantityType.getUnit()).isEqualTo(Units.BAR);
    assertThat(quantityType.as(DecimalType.class)).isEqualTo(new DecimalType(1.0));
  }

  @Test
  void checkImpulse() {
    TAValue value = new TAValue(AnalogUnit.IMPULSE.getIndex(), 100);

    assertThat(value.getValue()).isInstanceOf(Quantity.class)
      .isEqualTo(Quantities.getQuantity(100, AnalogUnit.IMPULSE.getUnit()));

    Quantity<?> quantity = (Quantity<?>) value.getValue();
    QuantityType<?> quantityType = new QuantityType<>(quantity.getValue(), quantity.getUnit());

    assertThat(quantityType.getUnit()).isEqualTo(AnalogUnit.IMPULSE.getUnit());
    assertThat(quantityType.as(DecimalType.class)).isEqualTo(new DecimalType(100));
  }

  // 40c6 => (c6) 19.8 time/auto; scale=0.1; 40=> mode (auto)
  @Test
  void checkTemperatureAndRas1() {
    TAValue value = new TAValue(ComplexUnit.RAS_TEMPERATURE.getIndex(), 0x40c6);

    assertThat(value.getValue()).isInstanceOf(List.class)
      .asList()
      .contains(Quantities.getQuantity(19.8, SIUnits.CELSIUS), atIndex(0))
      .contains(0, atIndex(1));
  }

  // 40b2 => (b2) 17.8 time/auto; scale=0.1; 40=> mode (auto)
  @Test
  void checkTemperatureAndRas2() {
    TAValue value = new TAValue(ComplexUnit.RAS_TEMPERATURE.getIndex(), 0x40b2);

    assertThat(value.getValue()).isInstanceOf(List.class)
      .asList()
      .contains(Quantities.getQuantity(17.8, SIUnits.CELSIUS), atIndex(0))
      .contains(0, atIndex(1));
  }

}