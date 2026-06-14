/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MeterValuesConfigAdapterTest {

  @Test
  void stripsTemperatureFirst() {
    assertThat(MeterValuesConfigAdapter.stripFirstFragile(
        "Energy.Active.Import.Register,Power.Active.Import,Current.Import,Voltage,Temperature"))
        .isEqualTo("Energy.Active.Import.Register,Power.Active.Import,Current.Import,Voltage");
  }

  @Test
  void stripsPowerOfferedWhenTemperatureAbsent() {
    assertThat(MeterValuesConfigAdapter.stripFirstFragile(
        "Energy.Active.Import.Register,Power.Offered,Current.Import,Voltage"))
        .isEqualTo("Energy.Active.Import.Register,Current.Import,Voltage");
  }

  @Test
  void stripsTemperatureBeforePowerOffered() {
    assertThat(MeterValuesConfigAdapter.stripFirstFragile(
        "Power.Offered,Temperature,Current.Import"))
        .isEqualTo("Power.Offered,Current.Import");
  }

  @Test
  void leavesUntouchedWhenNoFragileMeasurand() {
    assertThat(MeterValuesConfigAdapter.stripFirstFragile("Energy.Active.Import.Register,Voltage"))
        .isEqualTo("Energy.Active.Import.Register,Voltage");
  }

  @Test
  void handlesWhitespaceAroundCommas() {
    assertThat(MeterValuesConfigAdapter.stripFirstFragile("Voltage, Temperature, Current.Import"))
        .isEqualTo("Voltage,Current.Import");
  }

  @Test
  void handlesNullAndEmpty() {
    assertThat(MeterValuesConfigAdapter.stripFirstFragile(null)).isNull();
    assertThat(MeterValuesConfigAdapter.stripFirstFragile("")).isEmpty();
  }
}
