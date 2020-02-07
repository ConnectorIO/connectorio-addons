package org.connectorio.binding.efficiency.internal.ventilation.heatex;

import org.connectorio.binding.base.config.Configuration;

public class HeatExConfig implements Configuration {

  public String intakeTemperature;
  public String supplyTemperature;
  public String extractTemperature;
  public Long cycleTime = 60L;

}
