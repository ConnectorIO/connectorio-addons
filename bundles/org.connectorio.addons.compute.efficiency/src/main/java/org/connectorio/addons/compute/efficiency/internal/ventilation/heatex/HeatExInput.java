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
package org.connectorio.addons.compute.efficiency.internal.ventilation.heatex;

import org.openhab.core.items.Item;

public class HeatExInput {

  private final Item intakeTemperature;
  private final Item supplyTemperature;
  private final Item extractTemperature;

  public HeatExInput(Item intakeTemperature, Item supplyTemperature, Item extractTemperature) {
    this.intakeTemperature = intakeTemperature;
    this.supplyTemperature = supplyTemperature;
    this.extractTemperature = extractTemperature;
  }

  public Item getIntakeTemperature() {
    return intakeTemperature;
  }

  public Item getSupplyTemperature() {
    return supplyTemperature;
  }

  public Item getExtractTemperature() {
    return extractTemperature;
  }

}
