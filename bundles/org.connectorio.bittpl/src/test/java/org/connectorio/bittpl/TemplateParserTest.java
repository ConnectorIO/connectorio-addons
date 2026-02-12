/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.bittpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class TemplateParserTest {

  @Test
  void testParser() throws Exception {
    Template template = TemplateParser.parse("xAA xAB xAC b.... ...1");

    byte[] message = new byte[] {(byte) 0xAA, (byte) 0xAB, (byte) 0xAC, 0x01};
    boolean matches = template.matches(message);
    assertThat(matches).isTrue();

    byte[] output = template.toMessage();
    assertThat(output).isEqualTo(message);

    message = new byte[] {(byte) 0xAA, (byte) 0xAB, (byte) 0xAC, 0x00};
    matches = template.matches(message);
    assertThat(matches).isFalse();

    message = new byte[] {(byte) 0xAA, (byte) 0xAB, (byte) 0xAC, 0x01, 0x00};
    matches = template.matches(message);
    assertThat(matches).isTrue();
  }

  @Test
  void testEmptyInput() throws IOException {
    Template template = TemplateParser.parse("");
    assertThat(template.matches(new byte[0]))
      .isTrue();

    assertThat(template.toMessage())
      .isEqualTo(new byte[0]);
  }

  @Test
  void testInvalidHexInput() {
    assertThatThrownBy(() -> TemplateParser.parse("a"))
      .isInstanceOf(TemplateException.class)
      .hasMessageContaining("at 1:0");
  }

}