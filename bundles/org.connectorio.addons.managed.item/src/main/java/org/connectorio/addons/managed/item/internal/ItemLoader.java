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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.connectorio.addons.managed.item.internal.reader.XStreamItemReader;
import org.connectorio.addons.managed.item.model.GroupEntry;
import org.connectorio.addons.managed.item.model.ItemEntry;
import org.connectorio.addons.managed.item.model.Items;
import org.connectorio.addons.managed.item.model.LinkEntry;
import org.connectorio.addons.managed.item.model.MetadataEntry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component
public class ItemLoader {

  List<ServiceRegistration<?>> registrations = new ArrayList<>();

  @Activate
  public ItemLoader(BundleContext context, @Reference ItemBuilderFactory itemFactory) {
    File managed = new File(System.getProperty("openhab.userdata"), "managed");
    if (!managed.isDirectory() && managed.exists()) {
      managed.mkdirs();
    }

    File[] files = managed.listFiles(f -> f.getName().contains("item"));
    if (files == null) {
      return;
    }

    Set<Item> items = new LinkedHashSet<>();
    Set<Metadata> metadata = new LinkedHashSet<>();
    Set<ItemChannelLink> links = new LinkedHashSet<>();
    for (File file : files) {
      try {
        XStreamItemReader reader = new XStreamItemReader();
        Items parsedItems = reader.readFromXML(file.toURI().toURL());
        for (ItemEntry entry : parsedItems.getItems()) {
          items.add(create(itemFactory.newItemBuilder(entry.type, entry.name), entry));
          if (entry.metadata != null && !entry.metadata.isEmpty()) {
            for (Entry<String, MetadataEntry> meta : entry.metadata.entrySet()) {
              metadata.add(createMetadata(entry.name, meta));
            }
          }
          if (entry.channels != null && !entry.channels.isEmpty()) {
            for (LinkEntry channel : entry.channels) {
              links.add(createLink(entry.name, channel));
            }
          }
        }
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    registrations.add(context.registerService(ItemProvider.class, new XStreamItemProvider(items), new Hashtable<>()));
    registrations.add(context.registerService(MetadataProvider.class, new XStreamMetadataProvider(metadata), new Hashtable<>()));
    registrations.add(context.registerService(ItemChannelLinkProvider.class, new XStreamLinkProvider(links), new Hashtable<>()));
  }

  private ItemChannelLink createLink(String name, LinkEntry channel) {
    return new ItemChannelLink(name, new ChannelUID(channel.channel), new Configuration(channel.config));
  }

  private Metadata createMetadata(String name, Entry<String, MetadataEntry> meta) {
    return new Metadata(new MetadataKey(meta.getKey(), name), meta.getValue().value, meta.getValue().config);
  }

  @Deactivate
  void deactivate() {
    for (ServiceRegistration<?> registration : registrations) {
      registration.unregister();
    }
  }

  private Item create(ItemBuilder builder, ItemEntry item) {
    builder.withLabel(item.label);
    builder.withCategory(item.category);
    builder.withTags(item.tags);
    builder.withGroups(item.groups);

    if (item instanceof GroupEntry) {
      // TODO properly handle groups!
//      GroupItemEntry group = (GroupItemEntry) item;
//      builder.withBaseItem(group.baseItemType);
    }

    return builder.build();
  }

}
