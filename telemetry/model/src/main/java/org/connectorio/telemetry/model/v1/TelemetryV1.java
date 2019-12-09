package org.connectorio.telemetry.model.v1;

import java.util.Collections;
import java.util.Map;
import org.connectorio.telemetry.model.Telemetry;

public class TelemetryV1 implements Telemetry {

  private final long timestamp;
  private String classification;
  private Map<? extends String, ?> attributes;

  public TelemetryV1(String classification, Map<? extends String, ?> attributes) {
    this(System.currentTimeMillis(), classification, attributes);
  }

  public TelemetryV1(long timestamp, String classification, Map<? extends String, ?> attributes) {
    this.timestamp = timestamp;
    this.classification = classification;
    this.attributes = attributes;
  }

  @Override
  public final int getVersion() {
    return 1;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String getClassification() {
    return classification;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }
}
