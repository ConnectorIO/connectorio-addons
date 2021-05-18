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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

  @Test
  void testDigitalOutputCallbackWith8thBit() {
    trigger(callback, "01 00 00 00 00 00 00 00");
    for (int index = 1; index < 8; index++) {
      verify(device).updateDigital(index, false);
    }
    verify(device).updateDigital(8, true);
    for (int index = 9; index < 33; index++) {
      verify(device).updateDigital(index, false);
    }
    verifyNoMoreInteractions(device);
  }

  @Test
  void testDigitalOutputCallbackWith1stBit() {
    trigger(callback, "80 00 00 00 00 00 00 00");
    verify(device).updateDigital(1, true);
    for (int index = 2; index < 33; index++) {
      verify(device).updateDigital(index, false);
    }
    verifyNoMoreInteractions(device);
  }

  @Test
  void testByteArray() {
    trigger(callback, new byte[] {-128, 0, 0, 0, 0, 0, 0, 0});

    verify(device).updateDigital(1, true);
    for (int index = 2; index < 33; index++) {
      verify(device).updateDigital(index, false);
    }
    verifyNoMoreInteractions(device);
  }

  @Test
  void testWriteAndTrigger() throws ParseException {
    WriteBuffer buffer = new WriteBuffer(8);
    buffer.writeBit(true);
    for (int index = 2; index < 32; index++) {
      buffer.writeBit(false);
    }

    trigger(callback, buffer.getData());

    verify(device).updateDigital(1, true);
    for (int index = 2; index < 33; index++) {
      verify(device).updateDigital(index, false);
    }
    verifyNoMoreInteractions(device);
  }

}