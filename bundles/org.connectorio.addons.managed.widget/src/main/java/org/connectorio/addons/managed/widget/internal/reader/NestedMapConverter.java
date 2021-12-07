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
package org.connectorio.addons.managed.widget.internal.reader;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedMapConverter implements Converter {

  public static final Pattern DIGIT = Pattern.compile("^\\d+");

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    for (Entry<String, Object> entry: ((Map<String, Object>) source).entrySet()) {
      mapToXML(writer, entry, context);
    }
  }

  @SuppressWarnings("unchecked")
  private void mapToXML(HierarchicalStreamWriter writer, Entry<String, Object> entry,
    MarshallingContext context) {
    String key = entry.getKey();
    Matcher digit = DIGIT.matcher(key);
    if (digit.matches()) {
      key = "the_" + key;
    }
    if (key.startsWith("--")) {
      key = "the_" + key.substring(2);
    }
    writer.startNode(key);
    if (digit.matches()) {
      writer.addAttribute("class", "digit");
    }
    if (key.startsWith("__")) {
      writer.addAttribute("class", "suffix");
    }
    if (entry.getValue() instanceof Map) {
      Map<String, Object> nested = (Map<String, Object>) entry.getValue();
      for (Entry<String, Object> child : nested.entrySet()) {
        mapToXML(writer, child, context);
      }
    } else if (entry.getValue() instanceof List) {
      writer.addAttribute("class", "list");
      context.convertAnother(entry.getValue());
    } else {
      if (entry.getValue() != null) {
        writer.setValue(entry.getValue().toString());
     }
    }
    writer.endNode();
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return xmlToMap(reader, new LinkedHashMap<>(), context);
  }

  private Map<String, Object> xmlToMap(HierarchicalStreamReader reader, Map<String, Object> map,
    UnmarshallingContext context) {
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      String nodeName = reader.getNodeName();
      String clazz = reader.getAttribute("class");
      if (nodeName.startsWith("the_") && clazz.equals("digit")) {
        nodeName = nodeName.substring(4);
      }
      if (nodeName.startsWith("__") && clazz.equals("suffix")) {
        nodeName = "--" + nodeName.substring(2);
      }
      if (reader.hasMoreChildren()) {
        if (clazz != null && "list".equals(clazz)) {
          Object result = context.convertAnother(null, List.class);
          map.put(nodeName, result);
        } else{
          map.put(nodeName, xmlToMap(reader, new HashMap<>(), context));
        }
      } else {
        String value = reader.getValue();
        BigDecimal val;
        if (value != null) {
          map.put(nodeName, value);
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
