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
package org.connectorio.addons.managed.link.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.connectorio.addons.managed.link.internal.reader.XStreamLinkReader;
import org.connectorio.addons.managed.link.model.LinkEntry;
import org.connectorio.addons.managed.link.model.Links;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LinkLoader {

  private final Logger logger = LoggerFactory.getLogger(LinkLoader.class);
  private final BundleContext context;

  List<ServiceRegistration<?>> registrations = new ArrayList<>();

  @Activate
  public LinkLoader(BundleContext context) {
    this.context = context;
    File managed = new File(System.getProperty("openhab.userdata"), "managed");
    logger.info("Attempting to load link definitions from files located in {}", managed);
    if (!managed.isDirectory() && managed.exists()) {
      logger.trace("Directory {} not found, attempting to create", managed);
      managed.mkdirs();
    }

    File[] files = managed.listFiles(f -> f.getName().contains("link"));
    if (files == null) {
      logger.info("No files matching *thing* name found in {}", managed);
      return;
    }

    Set<ItemChannelLink> links = new LinkedHashSet<>();
    for (File file : files) {
      XStreamLinkReader reader = new XStreamLinkReader();
      Links parsedItems;
      try {
        parsedItems = reader.readFromXML(file.toURI().toURL());
        logger.info("Successfully read {} links from {}", parsedItems == null ? 0 : parsedItems.getLinks().size(), file);
      } catch (Exception e) {
        logger.error("Failed to parse document {}", file, e);
        continue;
      }

      for (LinkEntry entry : parsedItems.getLinks()) {
        links.add(createLink(entry.getItem(), entry));
      }
    }

    registrations.add(context.registerService(ItemChannelLinkProvider.class, new XStreamLinkProvider(links), new Hashtable<>()));
  }

  private ItemChannelLink createLink(String name, LinkEntry channel) {
    return new ItemChannelLink(name, new ChannelUID(channel.getChannel()), new Configuration(channel.getConfig()));
  }

  @Deactivate
  void deactivate() {
    for (ServiceRegistration<?> registration : registrations) {
      registration.unregister();
    }
  }

}
