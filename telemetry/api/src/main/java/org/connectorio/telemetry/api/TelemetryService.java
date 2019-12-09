package org.connectorio.telemetry.api;

/**
 * Telemetry service supports two operations - sending an telemetry and obtaining telemetry identifier.
 *
 * Because telemetry in environments such as openHAB is run on third party infrastructure it can be easily spoofed and
 * spammed with scripted devices. In order to avoid that Telemetry reporting requires device token which can be obtained
 * via pairing servlet.
 */
public interface TelemetryService {

  TelemetryClientId generateId();

  void upload(TelemetryClientId id, MeteredValue<?> measure);

  class TelemetryClientId {
    String id;
  }

  class MeteredValue<T extends Measured> {

  }

  interface Measured {

  }

}
