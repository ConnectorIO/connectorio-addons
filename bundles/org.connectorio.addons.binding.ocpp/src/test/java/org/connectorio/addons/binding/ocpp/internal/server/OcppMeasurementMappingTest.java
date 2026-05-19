package org.connectorio.addons.binding.ocpp.internal.server;

import static org.assertj.core.api.Assertions.assertThat;

import eu.chargetime.ocpp.model.core.SampledValue;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.junit.jupiter.api.Test;

class OcppMeasurementMappingTest {

  private static SampledValue sample(String measurand, String phase) {
    SampledValue v = new SampledValue();
    v.setMeasurand(measurand);
    if (phase != null) {
      v.setPhase(phase);
    }
    return v;
  }

  @Test
  void getReturnsAggregateChannelForKnownMeasurand() {
    assertThat(OcppMeasurementMapping.get("Current.Import"))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT);
    assertThat(OcppMeasurementMapping.get("Voltage"))
        .isEqualTo(OcppBindingConstants.VOLTAGE);
  }

  @Test
  void getStripsPhaseSuffixSoVendorFormResolvesToAggregate() {
    assertThat(OcppMeasurementMapping.get("Current.Import.L1"))
        .isEqualTo(OcppBindingConstants.CURRENT_IMPORT);
    assertThat(OcppMeasurementMapping.get("Voltage.L3"))
        .isEqualTo(OcppBindingConstants.VOLTAGE);
  }

  @Test
  void getReturnsNullForUnknown() {
    assertThat(OcppMeasurementMapping.get("NotAMeasurand")).isNull();
    assertThat(OcppMeasurementMapping.get(null)).isNull();
  }

  @Test
  void channelsForReturnsAggregateOnlyWhenNoPhase() {
    assertThat(OcppMeasurementMapping.channelsFor(sample("Current.Import", null)))
        .containsExactly(OcppBindingConstants.CURRENT_IMPORT);
    assertThat(OcppMeasurementMapping.channelsFor(sample("Power.Active.Import", null)))
        .containsExactly(OcppBindingConstants.POWER_ACTIVE_IMPORT);
  }

  @Test
  void channelsForReturnsBothAggregateAndPerPhaseWhenPhaseFieldSet() {
    assertThat(OcppMeasurementMapping.channelsFor(sample("Current.Import", "L1")))
        .containsExactly(
            OcppBindingConstants.CURRENT_IMPORT,
            OcppBindingConstants.CURRENT_IMPORT_L1);
    assertThat(OcppMeasurementMapping.channelsFor(sample("Voltage", "L3")))
        .containsExactly(
            OcppBindingConstants.VOLTAGE,
            OcppBindingConstants.VOLTAGE_L3);
  }

  @Test
  void channelsForReadsPhaseFromVendorSuffixWhenFieldMissing() {
    assertThat(OcppMeasurementMapping.channelsFor(sample("Current.Import.L2", null)))
        .containsExactly(
            OcppBindingConstants.CURRENT_IMPORT,
            OcppBindingConstants.CURRENT_IMPORT_L2);
    assertThat(OcppMeasurementMapping.channelsFor(sample("Voltage.L1", null)))
        .containsExactly(
            OcppBindingConstants.VOLTAGE,
            OcppBindingConstants.VOLTAGE_L1);
  }

  @Test
  void channelsForNormalisesPhaseWithLineNeutralSuffix() {
    // OCPP allows phases like "L1-N", "L1-L2"; only the leading L? identifies the phase.
    assertThat(OcppMeasurementMapping.channelsFor(sample("Voltage", "L1-N")))
        .containsExactly(
            OcppBindingConstants.VOLTAGE,
            OcppBindingConstants.VOLTAGE_L1);
    assertThat(OcppMeasurementMapping.channelsFor(sample("Voltage", "L2-L3")))
        .containsExactly(
            OcppBindingConstants.VOLTAGE,
            OcppBindingConstants.VOLTAGE_L2);
  }

  @Test
  void channelsForSkipsPerPhaseForMeasurandsWithoutPerPhaseSplit() {
    // OCPP 1.6 doesn't define per-phase variants for Power/Energy aggregates.
    assertThat(OcppMeasurementMapping.channelsFor(sample("Power.Active.Import", "L1")))
        .containsExactly(OcppBindingConstants.POWER_ACTIVE_IMPORT);
    assertThat(OcppMeasurementMapping.channelsFor(sample("Energy.Active.Import.Register", "L1")))
        .containsExactly(OcppBindingConstants.ENERGY_ACTIVE_IMPORT);
  }

  @Test
  void channelsForReturnsEmptyForUnknownOrNull() {
    assertThat(OcppMeasurementMapping.channelsFor(sample("NotAMeasurand", null))).isEmpty();
    assertThat(OcppMeasurementMapping.channelsFor(null)).isEmpty();
    assertThat(OcppMeasurementMapping.channelsFor(sample(null, null))).isEmpty();
  }

  @Test
  void channelsForIgnoresInvalidPhase() {
    // Bogus phase string falls back to aggregate-only.
    assertThat(OcppMeasurementMapping.channelsFor(sample("Current.Import", "X")))
        .containsExactly(OcppBindingConstants.CURRENT_IMPORT);
    assertThat(OcppMeasurementMapping.channelsFor(sample("Current.Import", "L9")))
        .containsExactly(OcppBindingConstants.CURRENT_IMPORT);
  }
}
