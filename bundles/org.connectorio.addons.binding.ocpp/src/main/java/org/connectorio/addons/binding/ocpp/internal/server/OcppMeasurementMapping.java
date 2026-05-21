package org.connectorio.addons.binding.ocpp.internal.server;

import static java.util.Map.entry;

import eu.chargetime.ocpp.model.core.SampledValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  // Measurands that have per-phase variants on 3-phase chargers. OCPP 1.6
  // doesn't define per-phase forms of Power/Energy aggregates, so only
  // Current.Import and Voltage are split.
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

  // Vendor-suffix form: "Current.Import.L1" with no phase field set.
  // Captured group holds the full "Lx" token so the same string can be used
  // both for stripping and for resolving the phase key in PER_PHASE.
  private final static Pattern PHASE_SUFFIX = Pattern.compile("\\.(L[1-3])$");

  /**
   * Resolve every channel that should receive an update for a given
   * SampledValue. The caller doesn't need to know about phase strings or
   * vendor quirks — this method is the single point of decision.
   *
   * <p>Returns:</p>
   * <ul>
   *   <li>empty list — unknown measurand</li>
   *   <li>aggregate channel only — measurand has no per-phase split, or no
   *       phase can be inferred</li>
   *   <li>aggregate + per-phase channel — when a phase is identified from
   *       either the {@code SampledValue.phase} field or a {@code .L1}/{@code .L2}/{@code .L3}
   *       suffix on the measurand. Updating both gives the user free choice
   *       of which channel to link in the item file.</li>
   * </ul>
   */
  public static List<UID> channelsFor(SampledValue sample) {
    if (sample == null || sample.getMeasurand() == null) {
      return List.of();
    }
    String base = stripPhaseSuffix(sample.getMeasurand());
    List<UID> channels = new ArrayList<>(2);

    ChannelRef aggregate = MAPPING.get(base);
    if (aggregate != null) {
      channels.add(aggregate);
    }

    String phase = resolvePhase(sample);
    if (phase != null) {
      Map<String, ChannelRef> perPhase = PER_PHASE.get(base);
      if (perPhase != null) {
        ChannelRef phaseChannel = perPhase.get(phase);
        if (phaseChannel != null) {
          channels.add(phaseChannel);
        }
      }
    }
    return channels;
  }

  /**
   * Direct measurand → channel lookup. Kept for callers that need to resolve
   * a channel from a measurand name alone; for live MeterValues handling
   * prefer {@link #channelsFor(SampledValue)}.
   */
  public static UID get(String measurement) {
    if (measurement == null) return null;
    return MAPPING.get(stripPhaseSuffix(measurement));
  }

  private static String resolvePhase(SampledValue sample) {
    // Standard form: phase field set, possibly with line-neutral suffix like "L1-N".
    String phase = sample.getPhase();
    if (phase != null && phase.length() >= 2) {
      String head = phase.substring(0, 2);
      if ("L1".equals(head) || "L2".equals(head) || "L3".equals(head)) {
        return head;
      }
    }
    // Vendor-suffix form: phase encoded in the measurand name itself.
    Matcher m = PHASE_SUFFIX.matcher(sample.getMeasurand());
    return m.find() ? m.group(1) : null;
  }

  private static String stripPhaseSuffix(String measurand) {
    return PHASE_SUFFIX.matcher(measurand).replaceAll("");
  }

}
