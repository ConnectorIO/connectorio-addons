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
package org.connectorio.addons.managed.thing.internal;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.managed.thing.internal.reader.XStreamThingReader;
import org.connectorio.addons.managed.thing.model.BridgeEntry;
import org.connectorio.addons.managed.thing.model.ChannelEntry;
import org.connectorio.addons.managed.thing.model.ThingEntry;
import org.connectorio.addons.managed.thing.model.Things;
import org.connectorio.addons.managed.xstream.SimpleProvider;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ThingLoader {

  private final Logger logger = LoggerFactory.getLogger(ThingLoader.class);
  private final BundleContext context;
  private final ThingTypeRegistry thingTypeRegistry;
  private final ChannelTypeRegistry channelTypeRegistry;
  private final ConfigDescriptionRegistry configDescriptionRegistry;

  private final Map<ServiceRegistration<?>, SimpleProvider<?>> registrations = new ConcurrentHashMap<>();

  @Activate
  public ThingLoader(BundleContext context, @Reference ThingTypeRegistry thingTypeRegistry, @Reference ChannelTypeRegistry channelTypeRegistry,
    @Reference ConfigDescriptionRegistry configDescriptionRegistry, @Reference ReadyService readyService) {
    this.context = context;
    this.thingTypeRegistry = thingTypeRegistry;
    this.channelTypeRegistry = channelTypeRegistry;
    this.configDescriptionRegistry = configDescriptionRegistry;
    File managed = new File(System.getProperty("openhab.userdata"), "managed");
    logger.info("Attempting to load thing definitions from files located in {}", managed);
    if (!managed.isDirectory() && managed.exists()) {
      logger.trace("Directory {} not found, attempting to create", managed);
      managed.mkdirs();
    }

    File[] files = managed.listFiles(f -> f.getName().contains("thing"));
    if (files == null) {
      logger.info("No files containing 'thing' in name found in {}", managed);
      return;
    }

    Set<Thing> things = new LinkedHashSet<>();
    for (File file : files) {
      XStreamThingReader reader = new XStreamThingReader();
      XStream stream = reader.getXStream();
      Things parsedItems;
      try {
        parsedItems = reader.readFromXML(file.toURI().toURL());
        logger.info("Successfully read {} things from {}", parsedItems == null ? 0 : parsedItems.getThings().size(), file);
      } catch (Exception e) {
        logger.error("Failed to parse document {}", file, e);
        continue;
      }

      for (ThingEntry entry : parsedItems.getThings()) {
        ThingBuilder builder;
        if (entry instanceof BridgeEntry) {
          builder = BridgeBuilder.create(new ThingTypeUID(entry.getType()), new ThingUID(entry.getId()));
        } else {
           builder = ThingBuilder.create(new ThingTypeUID(entry.getType()), new ThingUID(entry.getId()));
        }
        things.add(create(builder, entry));
      }
    }

    XStreamThingProvider thingProvider = new XStreamThingProvider(things);
    registrations.put(context.registerService(ThingProvider.class, thingProvider, new Hashtable<>()), thingProvider);

    readyService.markReady(new ReadyMarker("co7io-managed", "thing"));
  }

  @Deactivate
  void deactivate() {
    for (Entry<ServiceRegistration<?>, SimpleProvider<?>> registration : registrations.entrySet()) {
      registration.getValue().deactivate();
      registration.getKey().unregister();
    }
  }


  private Thing create(ThingBuilder thingBuilder, ThingEntry entry) {
    thingBuilder.withLabel(entry.getLabel());
    if (entry.getBridge() != null) {
      thingBuilder.withBridge(new ThingUID(entry.getBridge()));
    }
    thingBuilder.withConfiguration(new Configuration(entry.getConfig()));

    List<Channel> channels = new ArrayList<>();
    ThingType definition = thingTypeRegistry.getThingType(new ThingTypeUID(entry.getType()));
    if (definition != null) {
      for (ChannelDefinition ch : definition.getChannelDefinitions()) {
        ChannelType channelType = channelTypeRegistry.getChannelType(ch.getChannelTypeUID());
        if (channelType != null) {
          ChannelBuilder builder = ChannelBuilder.create(new ChannelUID(new ThingUID(entry.getId()), ch.getId()), channelType.getItemType());
          builder.withKind(channelType.getKind());
          builder.withType(ch.getChannelTypeUID());
          builder.withAutoUpdatePolicy(ch.getAutoUpdatePolicy());

          URI uri = channelType.getConfigDescriptionURI();
          if (uri != null) {
            Map<String, Object> config = new LinkedHashMap<>();
            ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(uri);
            if (configDescription != null) {
              for (ConfigDescriptionParameter param : configDescription.getParameters()) {
                if (param.isRequired() && param.getDefault() != null) {
                  config.put(param.getName(), param.getDefault());
                }
              }
            }
            builder.withConfiguration(new Configuration(config));
          }
          channels.add(builder.build());
        }
      }
    }

    if (entry.getChannels() != null && !entry.getChannels().isEmpty()) {
      for (ChannelEntry channel : entry.getChannels()) {
        ChannelBuilder builder = ChannelBuilder.create(new ChannelUID(channel.getId()));
        builder.withType(new ChannelTypeUID(channel.getType()));
        builder.withLabel(channel.getLabel());
        builder.withConfiguration(new Configuration(channel.getConfig()));
        channels.add(builder.build());
      }
    }
    thingBuilder.withChannels(channels);
    return thingBuilder.build();
  }

}
