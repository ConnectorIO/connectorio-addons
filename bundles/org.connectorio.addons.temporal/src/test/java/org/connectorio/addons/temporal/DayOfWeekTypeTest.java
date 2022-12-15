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
package org.connectorio.addons.temporal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DayOfWeekTypeTest {

  @Test
  public void testStringConversion() {
    DayOfWeekType local = DayOfWeekType.MONDAY;

    String string = local.toFullString();
    assertThat(string).isNotNull().hasLineCount(1);

    DayOfWeekType reconstructed = DayOfWeekType.valueOf(string);
    assertThat(reconstructed).isNotNull().isEqualTo(local);
  }

}