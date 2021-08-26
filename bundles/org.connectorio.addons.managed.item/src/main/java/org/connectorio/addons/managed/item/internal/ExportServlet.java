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
package org.connectorio.addons.managed.item.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.connectorio.addons.managed.item.internal.reader.XStreamItemReader;
import org.connectorio.addons.managed.item.model.GroupEntry;
import org.connectorio.addons.managed.item.model.ItemEntry;
import org.connectorio.addons.managed.item.model.Items;
import org.connectorio.addons.managed.item.model.LinkEntry;
import org.connectorio.addons.managed.item.model.MetadataEntry;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = HttpServlet.class)
public class ExportServlet extends HttpServlet {

  private final Logger logger = LoggerFactory.getLogger(ExportServlet.class);
  private final List<ItemChannelLinkProvider> linkProviders = new CopyOnWriteArrayList<>();

  private final HttpService service;
  private final ItemRegistry itemRegistry;
  private final MetadataRegistry metadataRegistry;

  @Activate
  public ExportServlet(@Reference HttpService service, @Reference ItemRegistry itemRegistry, @Reference MetadataRegistry metadataRegistry) throws ServletException, NamespaceException {
    this.service = service;
    this.itemRegistry = itemRegistry;
    this.metadataRegistry = metadataRegistry;
    service.registerServlet("/manage/export/item", this, new Hashtable<>(), null);
  }

  @Deactivate
  public void deactivate() {
    try {
      service.unregister("/manage/export/item");
    } catch (IllegalArgumentException e) {
      logger.debug("Failed to unregister export servlet, did it fail while registering?", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("Content-Type", "text/xml; charset=UTF-8");

    Map<String, Map<MetadataKey, Metadata>> metadataMap = new LinkedHashMap<>();
    for (Metadata metadata : metadataRegistry.getAll()) {
      if (!metadataMap.containsKey(metadata.getUID().getItemName())) {
        metadataMap.put(metadata.getUID().getItemName(), new LinkedHashMap<>());
      }
      metadataMap.get(metadata.getUID().getItemName()).put(metadata.getUID(), metadata);
    }
    Map<String, Set<ItemChannelLink>> linkMap = new LinkedHashMap<>();
    for (ItemChannelLinkProvider provider : linkProviders) {
      for (ItemChannelLink link : provider.getAll()) {
        if (!linkMap.containsKey(link.getItemName())) {
          linkMap.put(link.getItemName(), new LinkedHashSet<>());
        }
        linkMap.get(link.getItemName()).add(link);
      }
    }

    List<ItemEntry> items = new ArrayList<>();
    for (Item item : simplySorted()) {
      ItemEntry entry = "group".equalsIgnoreCase(item.getType()) ? new GroupEntry() : new ItemEntry();
      items.add(entry);
      entry.setCategory(item.getCategory());
      entry.setTags(item.getTags());
      entry.setGroups(item.getGroupNames());
      entry.setType(item.getType());
      entry.setName(item.getName());
      entry.setLabel(item.getLabel());
      if (metadataMap.containsKey(item.getName())) {
        Map<String, MetadataEntry> metadata = new LinkedHashMap<>();
        entry.setMetadata(metadata);
        for (Entry<MetadataKey, Metadata> meta : metadataMap.get(item.getName()).entrySet()) {
          MetadataEntry metadataEntry = new MetadataEntry(meta.getValue().getValue(), meta.getValue().getConfiguration());
          metadata.put(meta.getKey().getNamespace(), metadataEntry);
        }
      }
      if (linkMap.containsKey(item.getName())) {
        Set<ItemChannelLink> boundChannels = linkMap.get(item.getName());
        if (!boundChannels.isEmpty()) {
          Set<LinkEntry> channels = new LinkedHashSet<>();
          entry.setChannels(channels);
          for (ItemChannelLink channel : boundChannels) {
            LinkEntry link = new LinkEntry();
            channels.add(link);
            link.setChannel(channel.getLinkedUID().getAsString());
            Map<String, Object> properties = channel.getConfiguration().getProperties();
            if (!properties.isEmpty()) {
              link.setConfig(properties);
            }
          }
        }
      }
    }

    XStreamItemReader reader = new XStreamItemReader();
    resp.getWriter().write(reader.write(new Items(items)));

  }

  private Collection<Item> simplySorted() {
    Set<Item> items = new TreeSet<>(Comparator.comparing(Item::getName));
    items.addAll(itemRegistry.getAll());
    return items;
  }

  private Collection<Item> sorted() {
    Map<String, Set<String>> item2group = new HashMap<>();
    Map<String, Set<String>> group2item = new HashMap<>();
    for (Item item : itemRegistry.getAll()) {
      String itemName = item.getName();
      if (!item2group.containsKey(itemName)) {
        item2group.put(itemName, new HashSet<>());
      }

      resolveGroups(item2group, group2item, item, itemName);
    }
    // Returns a negative integer,
    // zero, or a positive integer as the first argument is less than, equal
    // to, or greater than the second.<p>
    Comparator<Item> comparator = new Comparator<Item>() {
      @Override
      public int compare(Item o1, Item o2) {
        String o1name = o1.getName();
        String o2name = o2.getName();
        if (item2group.get(o1name).contains(o2name)) {
          return 1;
        }
        if (item2group.get(o2name).contains(o1name)) {
          return 1;
        }

        return o1name.compareTo(o2name);
      }

      @Override
      public boolean equals(Object obj) {
        return false;
      }
    };
    Set<Item> items = new TreeSet<>(comparator);
    items.addAll(itemRegistry.getAll());
    return items;
  }

  private void resolveGroups(Map<String, Set<String>> item2group, Map<String, Set<String>> group2item, Item item, String itemName) {
    for (String group : item.getGroupNames()) {
      if (!item2group.containsKey(itemName)) {
        item2group.put(itemName, new HashSet<>());
      }
      item2group.get(itemName).add(group);
      if (!group2item.containsKey(group)) {
        group2item.put(group, new HashSet<>());
      }
      group2item.get(group).add(itemName);
      Item groupItem = itemRegistry.get(group);
      if (groupItem != null) {
        resolveGroups(item2group, group2item, groupItem, itemName);
      }
    }
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE)
  public void addProvider(ItemChannelLinkProvider provider) {
    this.linkProviders.add(provider);

  }

  public void removeProvider(ItemChannelLinkProvider provider) {
    this.linkProviders.remove(provider);
  }

}
