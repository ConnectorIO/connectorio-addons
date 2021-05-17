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
package org.connectorio.addons.io.proxy.http.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public class FilteredDictionary<V> extends Dictionary<String, V> {

  private final Dictionary<String, V> delegate;

  public FilteredDictionary(String prefix, Dictionary<String, V> delegate) {
    this.delegate = filter(prefix, delegate);
  }

  private static <V> Dictionary<String, V> filter(String prefix, Dictionary<String, V> delegate) {
    Map<String, V> result = new LinkedHashMap<>();

    Enumeration<String> keys = delegate.keys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      if (key.startsWith(prefix)) {
        result.put(key.substring(prefix.length()), delegate.get(key));
      }
    }

    return new Hashtable<>(result);
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public Enumeration<String> keys() {
    return delegate.keys();
  }

  @Override
  public Enumeration<V> elements() {
    return delegate.elements();
  }

  @Override
  public V get(Object key) {
    return delegate.get(key);
  }

  @Override
  public V put(String key, V value) {
    return delegate.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return delegate.remove(key);
  }

}
