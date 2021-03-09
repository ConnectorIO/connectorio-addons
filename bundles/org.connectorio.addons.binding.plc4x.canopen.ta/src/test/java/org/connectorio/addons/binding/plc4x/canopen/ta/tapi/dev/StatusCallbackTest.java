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
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatusCallbackTest extends AbstractCallbackTest {

  private StatusCallback callback;

  @Mock
  private Consumer<Boolean> consumer;

  @BeforeEach
  void setUp() {
    callback = new StatusCallback(9, consumer);
  }

  @Test
  void testSuccessfulLogin() {
    trigger(callback, "89 80 12 01 49 06 00 00");
    verify(consumer).accept(true);
  }

  @Test
  void testFailedLogin() {
    trigger(callback, "89 80 12 01 49 06 00 80");
    verify(consumer).accept(false);
  }

  @Test
  void testUnknownStatus() {
    trigger(callback, "89 80 12 01 49 06 00 FF");
    verify(consumer).accept(false);
  }

  @Test
  void testDifferentClient() {
    trigger(callback, "89 80 12 01 48 06 00 00");
    verifyNoInteractions(consumer);
  }

}