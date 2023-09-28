package org.connectorio.addons.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.spi.SystemOfUnits;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.unit.SIUnits;

public class StubUnitProvider implements UnitProvider {

  private Map<Class<? extends Quantity<?>>, Map<SystemOfUnits, Unit<? extends Quantity<?>>>> dimensions = new HashMap<>();
  private final SystemOfUnits measurementSystem;

  public StubUnitProvider() {
    this(SIUnits.getInstance());
  }

  public StubUnitProvider(SystemOfUnits measurementSystem) {
    this.measurementSystem = measurementSystem;
  }

  @Override
  public <T extends Quantity<T>> Unit<T> getUnit(Class<T> aClass) throws IllegalArgumentException {
    return (Unit<T>) dimensions.get(aClass).get(measurementSystem);
  }

  @Override
  public SystemOfUnits getMeasurementSystem() {
    return measurementSystem;
  }

  @Override
  public Collection<Class<? extends Quantity<?>>> getAllDimensions() {
    return dimensions.keySet();
  }

  public <T extends Quantity<T>> StubUnitProvider withDimension(Class<T> energyClass, Unit<T> unit) {
    dimensions.put(energyClass, Map.of(measurementSystem, unit));
    return this;
  }
}
