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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterReading;
import org.connectorio.addons.binding.relayweblog.client.dto.SubMeterReading;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubMeterWeblogClientTest {

  @Mock
  WeblogClient client;

  @Test
  void verifySubMeterDetection() {
    SubMeterWeblogClient weblog = new SubMeterWeblogClient(this.client);

    when(client.getReadings("1")).thenReturn(Arrays.asList(
      new MeterReading(RelayWeblogBindingConstants.ENHANCED_IDENTIFICATION_FIELD, "1234", ""),
      new MeterReading("Volume", "1234", "m^3 "),
      new MeterReading(RelayWeblogBindingConstants.ENHANCED_IDENTIFICATION_FIELD, "4321", ""),
      new MeterReading("Volume", "4321", "m^3 ")
    ));

    List<MeterReading> list = weblog.getReadings("1");
    assertThat(list).isNotEmpty()
      .hasSize(2)
      .containsOnly(
        new SubMeterReading("Volume", "m^3", "1234", "1234"),
        new SubMeterReading("Volume", "m^3", "4321", "4321")
      );
  }

}