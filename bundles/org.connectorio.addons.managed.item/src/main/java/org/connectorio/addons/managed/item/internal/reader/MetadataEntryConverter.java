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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.Map;
import org.connectorio.addons.managed.item.model.MetadataEntry;
import org.connectorio.addons.managed.xstream.NestedMapConverter;

/**
 * Converter which holds metadata "value" inside attribute.
 *
 * Rest of configuration is kept as an map.
 */
public class MetadataEntryConverter extends NestedMapConverter {

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String value = reader.getAttribute("value");
    Map unmarshal = (Map) super.unmarshal(reader, context);
    return new MetadataEntry(value, unmarshal);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    MetadataEntry meta = (MetadataEntry) source;
    if (meta != null && meta.getValue() != null) {
      writer.addAttribute("value", meta.getValue());
    }
    super.marshal(meta.getConfig(), writer, context);
  }

  @Override
  public boolean canConvert(Class type) {
    return MetadataEntry.class.isAssignableFrom(type);
  }
}
