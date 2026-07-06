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
package eu.chargetime.ocpp.model.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

/**
 * The local override of StopTransactionRequest must ACCEPT the payloads a meter-less/FreeMode
 * charge point (Phoenix CHARX without an energy meter) actually sends — upstream validation
 * rejects them with OccurenceConstraintViolation, the charger retries then gives up, and the
 * never-confirmed transaction wedges the connector (phantom transaction).
 */
class StopTransactionRequestToleranceTest {

  @Test
  void acceptsAStopWithoutMeterStop() {
    StopTransactionRequest request = new StopTransactionRequest();
    request.setTimestamp(ZonedDateTime.now());
    request.setTransactionId(3);
    // no meterStop — a charge point with no energy meter has nothing to report
    assertThat(request.validate()).isTrue();
  }

  @Test
  void acceptsAStopWithoutTransactionId() {
    StopTransactionRequest request = new StopTransactionRequest();
    request.setTimestamp(ZonedDateTime.now());
    request.setMeterStop(0);
    // no transactionId — a locally started (FreeMode) session has no CSMS-assigned id; the
    // handler already answers unknown transactions with an empty confirmation
    assertThat(request.validate()).isTrue();
  }

  @Test
  void acceptsTheRegularWellFormedStop() {
    StopTransactionRequest request = new StopTransactionRequest(0, ZonedDateTime.now(), 1);
    request.setIdTag("openhab");
    request.setReason(Reason.PowerLoss);
    assertThat(request.validate()).isTrue();
  }

  @Test
  void stillRejectsAStopWithoutTimestamp() {
    StopTransactionRequest request = new StopTransactionRequest();
    request.setMeterStop(0);
    request.setTransactionId(1);
    assertThat(request.validate()).isFalse();
  }

  @Test
  void remainsTransactionRelated() {
    assertThat(new StopTransactionRequest().transactionRelated()).isTrue();
  }
}
