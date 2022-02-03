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
package org.connectorio.addons.managed.item.internal.reader;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.managed.item.model.MetadataEntry;
import org.connectorio.addons.managed.xstream.NestedMapConverter;

public class MetadataMapConverter implements Converter {

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Map<String, MetadataEntry> metadata = (Map<String, MetadataEntry>) source;
    for (Entry<String, MetadataEntry> entry : metadata.entrySet()) {
      writer.startNode(entry.getKey());
      context.convertAnother(entry.getValue());
      writer.endNode();
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Map<String, MetadataEntry> metadata = new LinkedHashMap<>();
    if (reader.hasMoreChildren()) {
      reader.moveDown();
      String key = reader.getNodeName();
      MetadataEntry value = (MetadataEntry) context.convertAnother(null, MetadataEntry.class);
      metadata.put(key, value);
      reader.moveUp();
    }
    return metadata;
  }

  @Override
  public boolean canConvert(Class type) {
    return Map.class.isAssignableFrom(type);
  }

}
