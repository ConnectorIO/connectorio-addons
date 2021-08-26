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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.connectorio.addons.managed.thing.internal.reader.XStreamThingReader;
import org.connectorio.addons.managed.thing.model.BridgeEntry;
import org.connectorio.addons.managed.thing.model.ChannelEntry;
import org.connectorio.addons.managed.thing.model.ThingEntry;
import org.connectorio.addons.managed.thing.model.Things;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = HttpServlet.class)
public class ExportServlet extends HttpServlet {

  private final Logger logger = LoggerFactory.getLogger(ExportServlet.class);
  private final HttpService service;
  private final ThingRegistry thingRegistry;
  private final ThingTypeRegistry typeRegistry;

  @Activate
  public ExportServlet(@Reference HttpService service, @Reference ThingRegistry thingRegistry, @Reference ThingTypeRegistry typeRegistry) throws ServletException, NamespaceException {
    this.service = service;
    this.thingRegistry = thingRegistry;
    this.typeRegistry = typeRegistry;
    service.registerServlet("/manage/export/thing", this, new Hashtable<>(), null);
  }

  @Deactivate
  public void deactivate() {
    try {
      service.unregister("/manage/export/thing");
    } catch (IllegalArgumentException e) {
      logger.debug("Failed to unregister export servlet, did it fail while registering?", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("Content-Type", "text/xml; charset=UTF-8");

    List<ThingEntry> items = new ArrayList<>();
    for (Thing thing : sorted()) {
      ThingEntry entry = thing instanceof Bridge ? new BridgeEntry() : new ThingEntry();
      items.add(entry);

      entry.id = thing.getUID().getAsString();
      entry.type = thing.getThingTypeUID().getAsString();
      entry.label = thing.getLabel();
      if (thing.getBridgeUID() != null) {
        entry.bridge = thing.getBridgeUID().getAsString();
      }
      entry.config = thing.getConfiguration().getProperties();

      List<Channel> channels = thing.getChannels();
      List<String> extensibleChannelTypes = new ArrayList<>();
      ThingType type = typeRegistry.getThingType(thing.getThingTypeUID());
      if (type != null) {
        extensibleChannelTypes.addAll(type.getExtensibleChannelTypeIds());
      }

      if (!channels.isEmpty()) {
        entry.channels = new ArrayList<>();
        for (Channel channel : channels) {
          // make sure we only attach extensible channels which are defined by user, not by
          if (extensibleChannelTypes.contains(channel.getChannelTypeUID().getId())) {
            ChannelEntry che = new ChannelEntry();
            entry.channels.add(che);
            che.id = channel.getUID().getAsString();
            if (channel.getChannelTypeUID() != null) {
              che.type = channel.getChannelTypeUID().getAsString();
            }
//            che.kind = typeRegistry.getChannelType(channel).getKind() == ChannelKind.STATE ? null : ChannelKind.TRIGGER;
//            che. = typeRegistry.getChannelType(channel).getKind() == ChannelKind.STATE ? null : ChannelKind.TRIGGER;
            che.label = channel.getLabel();
            che.config = channel.getConfiguration().getProperties();
          }
        }
      }
    }

    XStreamThingReader reader = new XStreamThingReader();
    resp.getWriter().write(reader.write(new Things(items)));

  }

  private Collection<Thing> sorted() {
//    List<Thing> sorted = new ArrayList<>(thingRegistry.getAll());
//    Collections.sort(sorted, new Comparator<Thing>() {
//      @Override
//      public int compare(Thing o1, Thing o2) {
//        ThingUID bridge1uid = o1.getBridgeUID();
//        ThingUID bridge2uid = o2.getBridgeUID();
//        String o2uid = o2.getUID().getAsString();
//        String o1uid = o1.getUID().getAsString();
//        if (bridge1uid != null && bridge1uid.getAsString().contains(o2uid)) {
//          return -1;
//        }
//        if (bridge2uid != null && bridge2uid.getAsString().contains(o1uid)) {
//          return 1;
//        }
//
//        return o1uid.compareTo(o2uid);
//      }
//    });

    Set<Thing> sorted = new TreeSet<>(Comparator.comparing(thing -> thing.getUID().getAsString()));
    sorted.addAll(thingRegistry.getAll());
    return sorted;
  }
}
