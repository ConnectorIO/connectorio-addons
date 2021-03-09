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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalOutputCallbackTest extends AbstractCallbackTest {

  private DigitalOutputCallback callback;

  @Mock
  private TADevice device;

  @BeforeEach
  void setUp() {
    callback = new DigitalOutputCallback(device);
  }

  @Test
  void testDigitalOutputCallback() {
    trigger(callback, "00 00 00 00 00 00 00 00");

    for (int index = 1; index < 33; index++) {
      verify(device).updateDigital(index, false);
    }
    verifyNoMoreInteractions(device);
  }

  @Test
  void testDigitalOutputCallbackWithOneTrue() {
    trigger(callback, "00 00 00 01 00 00 00 00");
    for (int index = 1; index < 32; index++) {
      verify(device).updateDigital(index, false);
    }
    verify(device).updateDigital(32, true);
    verifyNoMoreInteractions(device);
  }

}