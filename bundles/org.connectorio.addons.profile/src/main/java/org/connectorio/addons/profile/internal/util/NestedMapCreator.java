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
package org.connectorio.addons.profile.internal.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

// little helper to twist keys such as {a.b.c=d, a.b.e=f} into map {a={b={c=d, e=f}}}.
public class NestedMapCreator {

  public Map<String, Object> toNestedMap(Map<String, Object> flattened) {
    Map<String, Object> nested = new LinkedHashMap<>();
    for (Entry<String, Object> entry : flattened.entrySet()) {
      if (entry.getKey().contains(".")) {
        String[] chunks = entry.getKey().split("\\.");
        toNestedMap(nested, chunks, entry.getValue());
        continue;
      }
      nested.put(entry.getKey(), entry.getValue());
    }
    return nested;
  }

  private void toNestedMap(Map<String, Object> nested, String[] chunks, Object value) {
    Map<String, Object> dest = nested;
    for (int i = 0; i < chunks.length - 1; i++) {
      Object tmp = dest.get(chunks[i]);
      if (tmp == null) {
        Map<String,Object> next = new LinkedHashMap<>();
        dest.put(chunks[i], next);
        dest = next;
        continue;
      }

      if (!(tmp instanceof Map)) {
        throw new IllegalStateException();
      }
      dest = (Map<String, Object>) tmp;
    }
    dest.put(chunks[chunks.length - 1], value);
  }

}