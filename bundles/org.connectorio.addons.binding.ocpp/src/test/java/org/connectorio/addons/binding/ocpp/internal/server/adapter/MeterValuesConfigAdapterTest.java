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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import eu.chargetime.ocpp.model.core.ConfigurationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.junit.jupiter.api.Test;

class MeterValuesConfigAdapterTest {

  @Test
  void stripsTheLastMeasurand() {
    assertThat(MeterValuesConfigAdapter.stripLast(
        "Energy.Active.Import.Register,Power.Active.Import,Current.Import,Voltage"))
        .isEqualTo("Energy.Active.Import.Register,Power.Active.Import,Current.Import");
  }

  @Test
  void stripsDownToEmptyOnceOnlyOneMeasurandIsLeft() {
    assertThat(MeterValuesConfigAdapter.stripLast("Voltage")).isEmpty();
  }

  @Test
  void handlesWhitespaceAroundCommas() {
    assertThat(MeterValuesConfigAdapter.stripLast("Voltage, Temperature, Current.Import"))
        .isEqualTo("Voltage,Temperature");
  }

  @Test
  void handlesNullAndEmpty() {
    assertThat(MeterValuesConfigAdapter.stripLast(null)).isNull();
    assertThat(MeterValuesConfigAdapter.stripLast("")).isEmpty();
  }

  /**
   * Fake sender: Rejects a MeterValuesSampledData/AlignedData ChangeConfiguration while its value
   * still contains {@code rejectedMeasurand}, Accepts everything else. Stands in for a charger that
   * only supports a subset of the configured measurands, without hardcoding which subset.
   */
  private static OcppSender rejectingSenderFor(String rejectedMeasurand, List<String> sentMeasurandValues) {
    return new OcppSender() {
      @Override
      @SuppressWarnings("unchecked")
      public <T extends Confirmation> CompletionStage<T> send(ChargerReference reference, Request request) {
        ChangeConfigurationRequest change = (ChangeConfigurationRequest) request;
        if ("MeterValuesSampledData".equals(change.getKey()) || "MeterValuesAlignedData".equals(change.getKey())) {
          sentMeasurandValues.add(change.getValue());
        }
        ConfigurationStatus status = change.getValue() != null && change.getValue().contains(rejectedMeasurand)
            ? ConfigurationStatus.Rejected : ConfigurationStatus.Accepted;
        return (CompletionStage<T>) CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(status));
      }
    };
  }

  @Test
  void negotiatesDownOnRejectAndCachesTheAcceptedSetPerCharger() {
    ChargerReference charx = new ChargerReference("charx");
    UUID session = UUID.randomUUID();
    OcppChargerSessionRegistry registry = mock(OcppChargerSessionRegistry.class);
    when(registry.getCharger(session)).thenReturn(charx);

    List<String> sentMeasurandValues = new ArrayList<>();
    MeterValuesConfigAdapter adapter = new MeterValuesConfigAdapter(registry,
        rejectingSenderFor("Temperature", sentMeasurandValues), 30,
        "Energy.Active.Import.Register,Temperature", 30);

    adapter.handleBootNotificationRequest(session, new BootNotificationRequest());

    assertThat(adapter.acceptedMeasurands.get("charx")).isEqualTo("Energy.Active.Import.Register");
  }

  @Test
  void aReconnectStartsFromTheCachedAcceptedSetInsteadOfRenegotiating() {
    ChargerReference charx = new ChargerReference("charx");
    UUID session = UUID.randomUUID();
    OcppChargerSessionRegistry registry = mock(OcppChargerSessionRegistry.class);
    when(registry.getCharger(session)).thenReturn(charx);

    List<String> sentMeasurandValues = new ArrayList<>();
    MeterValuesConfigAdapter adapter = new MeterValuesConfigAdapter(registry,
        rejectingSenderFor("Temperature", sentMeasurandValues), 30,
        "Energy.Active.Import.Register,Temperature", 30);

    adapter.handleBootNotificationRequest(session, new BootNotificationRequest()); // first boot: negotiates down
    adapter.handleBootNotificationRequest(session, new BootNotificationRequest()); // reconnect: reuses the cache

    // First boot tries the full configured list for both measurand keys, rejected both times, then
    // retries each down to the already-accepted set. The reconnect goes straight to that set for
    // both keys, first try, no further negotiation.
    assertThat(sentMeasurandValues).containsExactly(
        "Energy.Active.Import.Register,Temperature", "Energy.Active.Import.Register",
        "Energy.Active.Import.Register,Temperature", "Energy.Active.Import.Register",
        "Energy.Active.Import.Register", "Energy.Active.Import.Register");
  }

  @Test
  void aChargerThatAcceptsTheFullListIsCachedTooSoLaterBootsSkipRenegotiation() {
    ChargerReference wallbox = new ChargerReference("wallbox");
    UUID session = UUID.randomUUID();
    OcppChargerSessionRegistry registry = mock(OcppChargerSessionRegistry.class);
    when(registry.getCharger(session)).thenReturn(wallbox);

    List<String> sentMeasurandValues = new ArrayList<>();
    MeterValuesConfigAdapter adapter = new MeterValuesConfigAdapter(registry,
        rejectingSenderFor("Temperature", sentMeasurandValues), 30,
        "Energy.Active.Import.Register,Voltage", 30);

    adapter.handleBootNotificationRequest(session, new BootNotificationRequest());

    assertThat(adapter.acceptedMeasurands.get("wallbox")).isEqualTo("Energy.Active.Import.Register,Voltage");
  }
}
