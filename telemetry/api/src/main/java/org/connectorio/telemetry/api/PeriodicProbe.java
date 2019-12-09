package org.connectorio.telemetry.api;

import java.time.Period;

public interface PeriodicProbe extends Probe {

  Period getPeriod();

}
