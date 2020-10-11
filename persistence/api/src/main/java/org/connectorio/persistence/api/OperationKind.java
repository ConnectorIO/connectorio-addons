/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.persistence.api;

import java.time.Instant;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.HistoricItem;

public interface OperationKind<T> {

  interface RetrievalKind extends OperationKind<HistoricItem> {
    RetrievalKind MINIMUM = new RetrievalKind() {};
    RetrievalKind MAXIMUM = new RetrievalKind() {};
    RetrievalKind PREVIOUS_STATE = new RetrievalKind() {};
  }

  interface CalculationKind<T> extends OperationKind<T> {
    CalculationKind<Boolean> CHANGED = new CalculationKind<Boolean>() {};
    CalculationKind<Boolean> UPDATED = new CalculationKind<Boolean>() {};

    CalculationKind<DecimalType> DELTA = new CalculationKind<DecimalType>() {};
    CalculationKind<DecimalType> AVERAGE = new CalculationKind<DecimalType>() {};
    CalculationKind<DecimalType> EVOLUTION = new CalculationKind<DecimalType>() {};
    CalculationKind<DecimalType> SUM = new CalculationKind<DecimalType>() {};

    CalculationKind<Instant> LAST_UPDATE = new CalculationKind<Instant>() {};
  }

  OperationKind<Iterable<HistoricItem>> SCAN = new OperationKind<Iterable<HistoricItem>>() {};

}
