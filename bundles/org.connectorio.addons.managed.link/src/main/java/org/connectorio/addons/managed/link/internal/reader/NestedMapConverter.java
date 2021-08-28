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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NestedMapConverter implements Converter {

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    for (Entry<String, Object> entry: ((Map<String, Object>) source).entrySet()) {
      mapToXML(writer, entry);
    }
  }

  @SuppressWarnings("unchecked")
  private void mapToXML(HierarchicalStreamWriter writer, Entry<String, Object> entry) {
    writer.startNode(entry.getKey());
    if (entry.getValue() instanceof Map) {
      Map<String, Object> nested = (Map<String, Object>) entry.getValue();
      for (Entry<String, Object> child : nested.entrySet()) {
        mapToXML(writer, child);
      }
    } else {
      if (entry.getValue() != null) {
        writer.setValue(entry.getValue().toString());
     }
    }
    writer.endNode();
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return xmlToMap(reader, new LinkedHashMap<>());
  }

  private Map<String, Object> xmlToMap(HierarchicalStreamReader reader, Map<String, Object> map) {
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      if( reader.hasMoreChildren() ) {
        map.put(reader.getNodeName(), xmlToMap(reader, new HashMap<>()));
      } else {
        String value = reader.getValue();
        BigDecimal val;
        if (value != null) {
          if ("true".equals(value) || "false".equals(value)) {
            map.put(reader.getNodeName(), Boolean.parseBoolean(value));
          } else if ((val = decimal(value)) != null) {
            map.put(reader.getNodeName(), val);
          } else {
            map.put(reader.getNodeName(), value);
          }
        }
      }
      reader.moveUp();
    }
    return map;
  }

  private BigDecimal decimal(String value) {
    try {
      return new BigDecimal(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
  @Override
  public boolean canConvert(Class type) {
    return Map.class.isAssignableFrom(type);
  }

}
