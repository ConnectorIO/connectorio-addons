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

  public static UID get(String measurement) {
    if (MAPPING.containsKey(measurement)) {
      return MAPPING.get(measurement);
    }

    return null;
  }

}
