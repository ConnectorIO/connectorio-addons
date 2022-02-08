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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.managed.item.internal.reader.XStreamItemReader;
import org.connectorio.addons.managed.item.model.GroupEntry;
import org.connectorio.addons.managed.item.model.ItemEntry;
import org.connectorio.addons.managed.item.model.Items;
import org.connectorio.addons.managed.item.model.MetadataEntry;
import org.connectorio.addons.managed.link.model.BaseLinkEntry;
import org.connectorio.addons.managed.xstream.SimpleProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.GroupFunction;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataProvider;
import org.openhab.core.items.dto.GroupFunctionDTO;
import org.openhab.core.items.dto.ItemDTOMapper;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ItemLoader {

  private final Logger logger = LoggerFactory.getLogger(ItemLoader.class);
  private final ItemBuilderFactory itemFactory;

  private Map<ServiceRegistration<?>, SimpleProvider<?>> registrations = new ConcurrentHashMap<>();

  @Activate
  public ItemLoader(BundleContext context, @Reference ReadyService readyService, @Reference ItemBuilderFactory itemFactory) {
    this.itemFactory = itemFactory;

    File managed = new File(System.getProperty("openhab.userdata"), "managed");
    if (!managed.isDirectory() && managed.exists()) {
      managed.mkdirs();
    }

    File[] files = managed.listFiles(f -> f.getName().contains("item"));
    if (files == null) {
      return;
    }

    List<Item> items = new ArrayList<>();
    List<Metadata> metadata = new ArrayList<>();
    List<ItemChannelLink> links = new ArrayList<>();
    for (File file : files) {
      try {
        XStreamItemReader reader = new XStreamItemReader();
        Items parsedItems = reader.readFromXML(file.toURI().toURL());
        if (parsedItems == null || parsedItems.getItems() == null) {
          logger.info("No items found in file {}", file);
          continue;
        }
        logger.info("Successfully read {} items from {}", parsedItems == null ? 0 : parsedItems.getItems().size(), file);
        for (ItemEntry entry : parsedItems.getItems()) {
          items.add(create(itemFactory.newItemBuilder(entry.getType(), entry.getName()), entry));

          if (entry.getMetadata() != null && !entry.getMetadata().isEmpty()) {
            for (Entry<String, MetadataEntry> meta : entry.getMetadata().entrySet()) {
              if ("semantics".equalsIgnoreCase(meta.getKey())) {
                continue;
              }
              metadata.add(createMetadata(entry.getName(), meta));
            }
          }
          if (entry.getChannels() != null && !entry.getChannels().isEmpty()) {
            for (BaseLinkEntry channel : entry.getChannels()) {
              links.add(createLink(entry.getName(), channel));
            }
          }
        }
      } catch (MalformedURLException e) {
        logger.error("Could not read file {}", file, e);
      } catch (Exception e) {
        logger.error("Could not parse file {}", file, e);
      }
    }
    XStreamItemProvider itemProvider = new XStreamItemProvider(items);
    XStreamMetadataProvider metadataProvider = new XStreamMetadataProvider(metadata);
    XStreamLinkProvider linkProvider = new XStreamLinkProvider(links);

    registrations.put(context.registerService(ItemProvider.class, itemProvider, new Hashtable<>()), itemProvider);
    registrations.put(context.registerService(MetadataProvider.class, metadataProvider, new Hashtable<>()), metadataProvider);
    registrations.put(context.registerService(ItemChannelLinkProvider.class, linkProvider, new Hashtable<>()), linkProvider);

    readyService.markReady(new ReadyMarker("co7io-managed", "item"));
  }

  private ItemChannelLink createLink(String name, BaseLinkEntry channel) {
    return new ItemChannelLink(name, new ChannelUID(channel.getChannel()), new Configuration(channel.getConfig()));
  }

  private Metadata createMetadata(String name, Entry<String, MetadataEntry> meta) {
    return new Metadata(new MetadataKey(meta.getKey(), name), meta.getValue().getValue(), meta.getValue().getConfig());
  }

  @Deactivate
  void deactivate() {
    for (Entry<ServiceRegistration<?>, SimpleProvider<?>> registration : registrations.entrySet()) {
      registration.getValue().deactivate();
      registration.getKey().unregister();
    }
  }

  private Item create(ItemBuilder builder, ItemEntry item) {
    builder.withLabel(item.getLabel());
    builder.withCategory(item.getCategory());
    builder.withTags(item.getTags());
    builder.withGroups(item.getGroups());

    if (item instanceof GroupEntry) {
      // TODO properly handle groups!
      GroupEntry group = (GroupEntry) item;

      Item baseItem = null;
      if (group.getBaseItemType() != null) {
        baseItem = itemFactory.newItemBuilder(group.getBaseItemType(), item.getName()).build();
        builder.withBaseItem(baseItem);
      }

      GroupFunctionDTO dto = new GroupFunctionDTO();
      dto.name = group.getFunction() == null ? "EQUALITY" : group.getFunction();
      dto.params = group.getParameters() == null ? new String[0] : group.getParameters().toArray(new String[group.getParameters().size()]);

      GroupFunction function = createFunction(baseItem, dto);
      builder.withGroupFunction(function);
      if (group.getMembers() != null) {
        
      }
    }

    return builder.build();
  }

  private GroupFunction createFunction(Item baseItem, GroupFunctionDTO dto) {
    try {
      return ItemDTOMapper.mapFunction(baseItem, dto);
    } catch (NoSuchMethodError e) {
      // OSH
      //GroupFunction newFunctionBuilder(@Nullable Item baseItem, GroupFunctionDTO function);
      try {
        Method method = ItemBuilderFactory.class.getMethod("newFunctionBuilder", Item.class, GroupFunctionDTO.class);
        Object result = method.invoke(itemFactory, baseItem, dto);
        if (result instanceof GroupFunction) {
          return (GroupFunction) result;
        }
        logger.warn("Could not create function {}", dto.name);
      } catch (Exception ex) {
        logger.error("Could not create function {}", dto.name, e);
      }
    }
    return null;
  }

}
