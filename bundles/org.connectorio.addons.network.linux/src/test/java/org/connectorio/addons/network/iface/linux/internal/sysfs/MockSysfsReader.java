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
package org.connectorio.addons.network.iface.linux.internal.sysfs;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.connectorio.addons.network.iface.linux.internal.SysfsReader;

public class MockSysfsReader implements SysfsReader {

  private final String path;
  private final Map<String, List<String>> contents;

  public MockSysfsReader(Map<String, List<String>> contents) {
    this("/sys", contents);
  }

  public MockSysfsReader(String path, Map<String, List<String>> contents) {
    this.path = path;
    this.contents = contents;
  }

  @Override
  public List<String> read(String file) {
    String key = path + "/" + file;
    return contents.getOrDefault(key, Collections.emptyList());
  }

  @Override
  public Set<String> list() {
    Set<String> matching = new LinkedHashSet<>();
    for (String key : contents.keySet()) {
      if (key.startsWith(path)) {
        if (key.length() < path.length() + 1) {
          continue;
        }

        String entry = key.substring(path.length() + 1);
        if (!entry.contains("/")) {
          matching.add(entry);
        }
      }
    }
    return matching;
  }

  @Override
  public SysfsReader narrow(String ... path) {
    return new MockSysfsReader(this.path + "/" + Arrays.stream(path)
      .reduce((upper, lower) -> upper + "/" + lower)
      .orElse(""),
      contents
    );
  }

  void update(String path, List<String> content) {
    contents.put(path, content);
  }

}
