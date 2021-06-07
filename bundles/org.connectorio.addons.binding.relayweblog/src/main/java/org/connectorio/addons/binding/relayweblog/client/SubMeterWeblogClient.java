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
package org.connectorio.addons.binding.relayweblog.client;

import java.util.ArrayList;
import java.util.List;
import org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterInfo;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterReading;
import org.connectorio.addons.binding.relayweblog.client.dto.PrimaryMeterReading;
import org.connectorio.addons.binding.relayweblog.client.dto.SubMeterReading;

/**
 * Weblog client which can aggregate sub-meter readings.
 */
public class SubMeterWeblogClient implements WeblogClient {

  private final WeblogClient delegate;

  public SubMeterWeblogClient(WeblogClient delegate) {
    this.delegate = delegate;
  }

  @Override
  public void login(String passwordHash, SigningContext signingContext) {
    delegate.login(passwordHash, signingContext);
  }

  @Override
  public List<MeterInfo> getMeters() {
    return  delegate.getMeters();
  }

  @Override
  public List<MeterReading> getReadings(String id) {
    List<MeterReading> readings = new ArrayList<>();
    String subMeterId = null;
    for (MeterReading reading : delegate.getReadings(id)) {
      if (RelayWeblogBindingConstants.ENHANCED_IDENTIFICATION_FIELD.equalsIgnoreCase(reading.getName())) {
        subMeterId = reading.getValue();
        continue;
      }

      if (subMeterId != null) {
        readings.add(new SubMeterReading(reading, subMeterId));
      } else {
        readings.add(new PrimaryMeterReading(reading));
      }
    }
    return readings;
  }


}
