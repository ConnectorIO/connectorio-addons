package org.connectorio.addons.binding.ocpp.internal.server;

import static java.util.Map.entry;

import java.util.Map;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants.ChannelRef;
import org.openhab.core.thing.UID;

public class OcppMeasurementMapping {

  private final static Map<String, ChannelRef> MAPPING = Map.ofEntries(
    entry("Current.Export", OcppBindingConstants.CURRENT_EXPORT),
    entry("Current.Import", OcppBindingConstants.CURRENT_IMPORT),
    entry("Current.Offered", OcppBindingConstants.CURRENT_OFFERED),
    entry("Energy.Active.Export.Register", OcppBindingConstants.ENERGY_ACTIVE_EXPORT),
    entry("Energy.Active.Import.Register", OcppBindingConstants.ENERGY_ACTIVE_IMPORT),
    entry("Energy.Reactive.Export.Register", OcppBindingConstants.ENERGY_REACTIVE_EXPORT),
    entry("Energy.Reactive.Import.Register", OcppBindingConstants.ENERGY_REACTIVE_IMPORT),
    entry("Energy.Active.Export.Interval", OcppBindingConstants.ENERGY_ACTIVE_EXPORT_INTERVAL),
    entry("Energy.Active.Import.Interval", OcppBindingConstants.ENERGY_ACTIVE_IMPORT_INTERVAL),
    entry("Energy.Reactive.Export.Interval", OcppBindingConstants.ENERGY_REACTIVE_EXPORT_INTERVAL),
    entry("Energy.Reactive.Import.Interval", OcppBindingConstants.ENERGY_REACTIVE_IMPORT_INTERVAL),
    entry("Frequency", OcppBindingConstants.FREQUENCY),
    entry("Power.Active.Export", OcppBindingConstants.POWER_ACTIVE_EXPORT),
    entry("Power.Active.Import", OcppBindingConstants.POWER_ACTIVE_IMPORT),
    entry("Power.Factor", OcppBindingConstants.POWER_FACTOR),
    entry("Power.Offered", OcppBindingConstants.POWER_OFFERED),
    entry("Power.Reactive.Export", OcppBindingConstants.POWER_REACTIVE_EXPORT),
    entry("Power.Reactive.Import", OcppBindingConstants.POWER_REACTIVE_IMPORT),
    entry("RPM", OcppBindingConstants.RPM),
    entry("SoC", OcppBindingConstants.SOC),
    entry("Temperature", OcppBindingConstants.TEMPERATURE),
    entry("Voltage", OcppBindingConstants.VOLTAGE)
  );

  // Per-phase channels — populated only for measurands that meaningfully vary
  // per phase on 3-phase chargers. Power/Energy aggregates remain single-channel
  // (OCPP 1.6 doesn't define per-phase variants of those measurands).
  private final static Map<String, Map<String, ChannelRef>> PER_PHASE = Map.of(
    "Current.Import", Map.of(
      "L1", OcppBindingConstants.CURRENT_IMPORT_L1,
      "L2", OcppBindingConstants.CURRENT_IMPORT_L2,
      "L3", OcppBindingConstants.CURRENT_IMPORT_L3
    ),
    "Voltage", Map.of(
      "L1", OcppBindingConstants.VOLTAGE_L1,
      "L2", OcppBindingConstants.VOLTAGE_L2,
      "L3", OcppBindingConstants.VOLTAGE_L3
    )
  );

  public static UID get(String measurement) {
    if (measurement == null) return null;
    // Normalize vendor-suffix form ("Current.Import.L1" → "Current.Import")
    // for the aggregate lookup so the existing single-value channel still
    // receives updates.
    String base = stripPhaseSuffix(measurement);
    return MAPPING.get(base);
  }

  /**
   * Resolve a per-phase channel from a SampledValue's measurand + phase fields.
   *
   * Two cases handled:
   * <ol>
   *   <li>Standard OCPP form — measurand = "Current.Import", phase = "L1"
   *       (or "L1-N"/"L1-L2"; only the L? prefix matters).</li>
   *   <li>Vendor-suffix form — measurand = "Current.Import.L1" with phase
   *       null. Seen in Wallbox firmware 6.7.x. We strip the trailing
   *       ".L1"/"L2"/"L3" off the measurand to recover the phase.</li>
   * </ol>
   *
   * @return ChannelRef for the per-phase channel, or {@code null} if the
   *         measurand isn't one we track per phase or the phase is empty.
   */
  public static UID getPerPhase(String measurement, String phase) {
    if (measurement == null) return null;
    String base = measurement;
    String resolvedPhase = phase;

    // Vendor-suffix form: phase embedded in the measurand, no phase field.
    if (resolvedPhase == null || resolvedPhase.isEmpty()) {
      String suffix = extractPhaseSuffix(measurement);
      if (suffix == null) return null;
      base = measurement.substring(0, measurement.length() - suffix.length() - 1);
      resolvedPhase = suffix;
    }

    // Normalize "L1-N" / "L1-L2" style — only the leading "L?" identifies the phase.
    String phaseKey = normalizePhaseKey(resolvedPhase);
    if (phaseKey == null) return null;

    Map<String, ChannelRef> phases = PER_PHASE.get(base);
    return phases == null ? null : phases.get(phaseKey);
  }

  private static String stripPhaseSuffix(String measurand) {
    String suffix = extractPhaseSuffix(measurand);
    return suffix == null ? measurand : measurand.substring(0, measurand.length() - suffix.length() - 1);
  }

  private static String extractPhaseSuffix(String measurand) {
    int dot = measurand.lastIndexOf('.');
    if (dot <= 0 || dot == measurand.length() - 1) return null;
    String tail = measurand.substring(dot + 1);
    return ("L1".equals(tail) || "L2".equals(tail) || "L3".equals(tail)) ? tail : null;
  }

  private static String normalizePhaseKey(String phase) {
    if (phase == null || phase.length() < 2) return null;
    String head = phase.substring(0, 2);
    return ("L1".equals(head) || "L2".equals(head) || "L3".equals(head)) ? head : null;
  }

}
