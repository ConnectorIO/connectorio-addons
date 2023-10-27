/*
 * Copyright (C) 2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.link.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import org.connectorio.addons.link.LinkListener;
import org.connectorio.addons.link.LinkManager;
import org.openhab.core.common.registry.Registry;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

public class BaseChannelLinkManager implements LinkManager {

  public static final String ITEM_CHANNEL_LINK_REGISTRY_FILTER = "(objectClass=org.openhab.core.thing.link.ItemChannelLinkRegistry)";

  private final Map<Thing, List<LinkListener>> listeners = new ConcurrentHashMap<>();

  private final Set<ChannelUID> linkedChannels = new CopyOnWriteArraySet<>();

  private final Registry<ItemChannelLink, String> linkRegistry;
  private final Registry<Thing, ThingUID> thingRegistry;

  private final LinkRegistryListener linkRegistryListener;
  private final ThingRegistryListener thingRegistryListener;

  public BaseChannelLinkManager(Registry<ItemChannelLink, String> linkRegistry, ThingRegistry thingRegistry) {
    this.linkRegistry = linkRegistry;
    this.thingRegistry = thingRegistry;

    linkRegistryListener = new LinkRegistryListener();
    thingRegistryListener = new ThingRegistryListener();
    linkRegistry.addRegistryChangeListener(linkRegistryListener);
    thingRegistry.addRegistryChangeListener(thingRegistryListener);
    linkRegistry.getAll().forEach(linkRegistryListener::added);
    thingRegistry.getAll().forEach(thingRegistryListener::added);
  }

  @Deactivate
  public void deactivate() {
    linkRegistry.removeRegistryChangeListener(linkRegistryListener);
    thingRegistry.removeRegistryChangeListener(thingRegistryListener);
  }

  @Override
  public boolean isLinked(ChannelUID channelUID) {
    return linkedChannels.contains(channelUID);
  }

  @Override
  public boolean hasLinkedChannels(Thing thing) {
    return linkedChannels.stream().anyMatch(channel -> thing.getUID().equals(channel.getThingUID()));
  }

  @Override
  public void registerListener(Thing thing, LinkListener listener) {
    listeners.computeIfAbsent(thing, (t) -> new CopyOnWriteArrayList<>())
      .add(listener);
  }

  @Override
  public void deregisterListener(Thing thing, LinkListener listener) {
    List<LinkListener> linkListeners = listeners.get(thing);
    if (linkListeners != null) {
      linkListeners.remove(listener);
      if (linkListeners.isEmpty()) {
          listeners.remove(thing);
      }
    }
  }

  class LinkRegistryListener implements RegistryChangeListener<ItemChannelLink> {

    @Override
    public void added(ItemChannelLink element) {
      linkedChannels.add(element.getLinkedUID());
      Thing thing = thingRegistry.get(element.getLinkedUID().getThingUID());
      if (thing != null && listeners.containsKey(thing)) {
        listeners.get(thing).forEach(listener -> listener.linked(element.getLinkedUID()));
      }
    }

    @Override
    public void removed(ItemChannelLink element) {
      Thing thing = thingRegistry.get(element.getLinkedUID().getThingUID());
      if (thing != null && listeners.containsKey(thing)) {
        listeners.get(thing).forEach(listener -> listener.unlinked(element.getLinkedUID()));
      }
      linkedChannels.remove(element.getLinkedUID());
    }

    @Override
    public void updated(ItemChannelLink oldElement, ItemChannelLink element) {
    }
  }

  class ThingRegistryListener implements RegistryChangeListener<Thing> {

    @Override
    public void added(Thing element) {
    }

    @Override
    public void removed(Thing element) {

    }

    @Override
    public void updated(Thing oldElement, Thing element) {
    }
  }
}
