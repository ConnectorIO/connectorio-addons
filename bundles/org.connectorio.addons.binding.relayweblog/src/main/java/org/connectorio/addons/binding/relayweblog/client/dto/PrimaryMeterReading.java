/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.relayweblog.client.dto;

public class PrimaryMeterReading extends MeterReading {

  public PrimaryMeterReading(MeterReading reading) {
    this(reading.getName(), reading.getValue(), reading.getUnit());
  }

  public PrimaryMeterReading(String name, String value, String unit) {
    super(name, value, unit);
  }

  public String toString() {
    return "Primary " + super.toString();
  }

}
