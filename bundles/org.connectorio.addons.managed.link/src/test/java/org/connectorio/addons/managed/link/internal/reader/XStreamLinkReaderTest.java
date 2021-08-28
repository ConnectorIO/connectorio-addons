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
package org.connectorio.addons.managed.link.internal.reader;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.connectorio.addons.managed.link.model.Links;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XStreamLinkReaderTest {

  @TempDir
  static Path directory;

  @Test
  void testReader() throws Exception {
    XStreamLinkReader reader = new XStreamLinkReader();
    Links links = reader.readFromXML(getClass().getResource("/links.xml"));

    String value = reader.write(links);

    Path testFile = directory.resolve("test.xml");
    Files.write(testFile, value.getBytes());

    Links deserialized = reader.readFromXML(testFile.toUri().toURL());
    assertThat(deserialized).isEqualTo(links);
  }

}