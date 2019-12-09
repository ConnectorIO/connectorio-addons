package org.connectorio.telemetry.api;

import org.connectorio.telemetry.model.Telemetry;

public interface Probe {

  Telemetry report();

}
