package org.connectorio.binding.efficiency.internal.ventilation.heatex;

import org.eclipse.smarthome.core.items.Item;

public class HeatExInput {

  private final Item intakeTemperature;
  private final Item supplyTemperature;
  private final Item extractTemperature;

  public HeatExInput(Item intakeTemperature, Item supplyTemperature, Item extractTemperature) {
    this.intakeTemperature = intakeTemperature;
    this.supplyTemperature = supplyTemperature;
    this.extractTemperature = extractTemperature;
  }

  public Item getIntakeTemperature() {
    return intakeTemperature;
  }

  public Item getSupplyTemperature() {
    return supplyTemperature;
  }

  public Item getExtractTemperature() {
    return extractTemperature;
  }

}
