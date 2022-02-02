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
package org.connectorio.addons.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

public class TestingMetadataRegistry implements MetadataRegistry {

  private final List<RegistryChangeListener<Metadata>> listeners = new ArrayList<>();
  private final Map<MetadataKey, Metadata> meta = new LinkedHashMap<>();

  @Override
  public boolean isInternalNamespace(String namespace) {
    return namespace.startsWith(INTERNAL_NAMESPACE_PREFIX);
  }

  @Override
  public void removeItemMetadata(String item) {
    for (MetadataKey key : meta.keySet()) {
      if (key.getItemName().equals(item)) {
        remove(key);
      }
    }
  }

  @Override
  public Collection<Metadata> getAll() {
    return meta.values();
  }

  @Override
  public Stream<Metadata> stream() {
    return meta.values().stream();
  }

  @Override
  public Metadata get(MetadataKey metadataKey) {
    return meta.get(metadataKey);
  }

  @Override
  public Metadata add(Metadata metadata) {
    meta.put(metadata.getUID(), metadata);
    listeners.forEach(listener -> listener.removed(metadata));
    return metadata;
  }

  @Override
  public Metadata update(Metadata metadata) {
    Metadata older = meta.put(metadata.getUID(), metadata);
    if (older != null) {
      this.listeners.forEach(listener -> listener.updated(older, metadata));
    }
    return older;
  }

  @Override
  public Metadata remove(MetadataKey metadataKey) {
    Metadata element = meta.remove(metadataKey);
    if (element != null) {
      listeners.forEach(listener -> listener.removed(element));
      return element;
    }
    return null;
  }

  @Override
  public void removeRegistryChangeListener(RegistryChangeListener<Metadata> registryChangeListener) {
    this.listeners.remove(registryChangeListener);
  }

  @Override
  public void addRegistryChangeListener(RegistryChangeListener<Metadata> registryChangeListener) {
    this.listeners.add(registryChangeListener);
  }

  public static MetadataKey key(String item, String namespace) {
    return new MetadataKey(namespace, item);
  }

  public static MetadataBuilder builder(String item, String namespace) {
    return new MetadataBuilder(key(item, namespace));
  }

  public static Metadata metadata(MetadataKey key, String value, Map<String, Object> values) {
    return new Metadata(key, value, values);
  }

  public static class MetadataBuilder {

    private final MetadataKey key;
    private Map<String, Object> values = new LinkedHashMap<>();
    private String value;

    MetadataBuilder(MetadataKey key) {
      this.key = key;
    }

    public MetadataBuilder value(String value) {
      this.value = value;
      return this;
    }

    public MetadataBuilder add(String key, Object value) {
      this.values.put(key, value);
      return this;
    }

    public Metadata build() {
      return metadata(key, value, values);
    }
  }

}
