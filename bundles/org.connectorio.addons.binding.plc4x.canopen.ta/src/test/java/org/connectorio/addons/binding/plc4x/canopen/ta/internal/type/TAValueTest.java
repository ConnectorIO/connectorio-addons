package org.connectorio.addons.binding.plc4x.canopen.ta.internal.type;

import static org.assertj.core.api.Assertions.assertThat;

import javax.measure.Quantity;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
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

}