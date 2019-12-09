package org.connectorio.telemetry.model;

import java.util.Map;

/**
 * Telemetry structure definition.
 */
public interface Telemetry {

  int getVersion();
  long getTimestamp();
  String getClassification();
  Map<String, Object> getAttributes();


}
