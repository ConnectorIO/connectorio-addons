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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.managed.item.model.GroupEntry;
import org.connectorio.addons.managed.item.model.ItemEntry;
import org.connectorio.addons.managed.item.model.Items;
import org.connectorio.addons.managed.item.model.MetadataEntry;
import org.connectorio.addons.managed.link.model.BaseLinkEntry;
import org.openhab.core.config.xml.util.XmlDocumentReader;

public class XStreamItemReader extends XmlDocumentReader<Items> {

  private XStream xstream;

  public XStreamItemReader() {
    ClassLoader classLoader = XStreamItemReader.class.getClassLoader();
    if (classLoader != null) {
      super.setClassLoader(classLoader);
    }
  }

  @Override
  protected void registerConverters(XStream xstream) {
    xstream.registerConverter(new Converter() {
      private NestedMapConverter delegate = new NestedMapConverter();
      @Override
      public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        MetadataEntry metadata = (MetadataEntry) source;
        if (metadata.getValue() != null) {
          writer.addAttribute("value", metadata.getValue());
        }
        delegate.marshal(metadata.getConfig(), writer, context);
      }

      @Override
      public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String value = reader.getAttribute("value");
        Map<String, Object> config = (Map<String, Object>) delegate.unmarshal(reader, context);
        return new MetadataEntry(value, config);
      }

      @Override
      public boolean canConvert(Class type) {
        return MetadataEntry.class.isAssignableFrom(type);
      }
    });
    xstream.registerLocalConverter(ItemEntry.class, "metadata", new Converter() {
      @Override
      public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map<String, MetadataEntry> meta = (Map<String, MetadataEntry>) source;
        for (Entry<String, MetadataEntry> entry : meta.entrySet()) {
          writer.startNode(entry.getKey());
          context.convertAnother(entry.getValue());
          writer.endNode();
        }
      }

      @Override
      public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map<String, MetadataEntry> entries = new LinkedHashMap<>();
        while (reader.hasMoreChildren()) {
          reader.moveDown();
          entries.put(reader.getNodeName(), (MetadataEntry) context.convertAnother(reader, MetadataEntry.class));
          reader.moveUp();
        }
        return entries;
      }

      @Override
      public boolean canConvert(Class type) {
        return Map.class.isAssignableFrom(type);
      }
    });
    xstream.registerLocalConverter(BaseLinkEntry.class, "config", new NestedMapConverter());
  }

  @Override
  protected void registerAliases(XStream xstream) {
    xstream.alias("items", Items.class);
    xstream.addImplicitCollection(Items.class, "items");
    xstream.alias("item", ItemEntry.class);
    xstream.alias("group", GroupEntry.class);
    xstream.alias("link", BaseLinkEntry.class);
    xstream.useAttributeFor(ItemEntry.class, "name");
    xstream.useAttributeFor(ItemEntry.class, "label");
    xstream.useAttributeFor(ItemEntry.class, "category");
    xstream.addImplicitCollection(ItemEntry.class, "tags", "tag", String.class);
    //xstream.addImplicitMap(ItemEntry.class, "metadata", MetadataEntry.class);
    xstream.addImplicitCollection(ItemEntry.class, "groups", "group", String.class);
    xstream.addImplicitCollection(ItemEntry.class, "channels", "link", BaseLinkEntry.class);

    xstream.addImplicitCollection(GroupEntry.class, "members", "member", String.class);

    xstream.useAttributeFor(MetadataEntry.class, "value");
  }

  // OH!
  public void configureSecurity(XStream xstream) {
    this.xstream = xstream;
    xstream.allowTypes(new Class[] {
      Items.class, ItemEntry.class, GroupEntry.class, BaseLinkEntry.class, MetadataEntry.class
    });
  }

  // OSH!
  public void registerSecurity(XStream xStream) {
    configureSecurity(xStream);
  }


  public String write(Items config) {
    return xstream.toXML(config);
  }
}
