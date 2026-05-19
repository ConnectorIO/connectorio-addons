package org.connectorio.addons.binding.ocpp.internal.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.UID;

class OcppMeasurementMappingTest {

  @Test
  void aggregateLookupReturnsBaseChannel() {
    assertThat(OcppMeasurementMapping.get("Current.Import"))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT);
    assertThat(OcppMeasurementMapping.get("Voltage"))
        .isEqualTo(OcppBindingConstants.VOLTAGE);
  }

  @Test
  void aggregateLookupReturnsNullForUnknown() {
    assertThat(OcppMeasurementMapping.get("NotAMeasurand")).isNull();
    assertThat(OcppMeasurementMapping.get(null)).isNull();
  }

  @Test
  void vendorSuffixResolvesAggregateToBase() {
    // Wallbox FW 6.7.x emits "Current.Import.L1" with no phase field;
    // the aggregate channel should still get updated from the base measurand.
    assertThat(OcppMeasurementMapping.get("Current.Import.L1"))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT);
    assertThat(OcppMeasurementMapping.get("Voltage.L3"))
        .isEqualTo(OcppBindingConstants.VOLTAGE);
  }

  @Test
  void perPhaseFromStandardPhaseField() {
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import", "L1"))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT_L1);
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import", "L2"))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT_L2);
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import", "L3"))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT_L3);
    assertThat(OcppMeasurementMapping.getPerPhase("Voltage", "L1"))
        .isEqualTo(OcppBindingConstants.VOLTAGE_L1);
  }

  @Test
  void perPhaseFromVendorSuffixWhenPhaseFieldMissing() {
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import.L1", null))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT_L1);
    assertThat(OcppMeasurementMapping.getPerPhase("Voltage.L3", ""))
        .isEqualTo(OcppBindingConstants.VOLTAGE_L3);
  }

  @Test
  void perPhaseNormalisesPhaseWithLineNeutralSuffix() {
    // OCPP allows phases like "L1-N", "L1-L2"; only the leading L? identifies it.
    assertThat(OcppMeasurementMapping.getPerPhase("Voltage", "L1-N"))
        .isEqualTo(OcppBindingConstants.VOLTAGE_L1);
    assertThat(OcppMeasurementMapping.getPerPhase("Voltage", "L2-L3"))
        .isEqualTo(OcppBindingConstants.VOLTAGE_L2);
  }

  @Test
  void perPhaseReturnsNullForMeasurandsWithoutPerPhaseSplit() {
    // Power.Active.Import has no per-phase channels in OCPP 1.6.
    assertThat(OcppMeasurementMapping.getPerPhase("Power.Active.Import", "L1")).isNull();
    assertThat(OcppMeasurementMapping.getPerPhase("Energy.Active.Import.Register", "L1")).isNull();
  }

  @Test
  void perPhaseReturnsNullForBadPhase() {
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import", null)).isNull();
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import", "")).isNull();
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import", "X")).isNull();
    assertThat(OcppMeasurementMapping.getPerPhase("Current.Import", "L9")).isNull();
  }
}
