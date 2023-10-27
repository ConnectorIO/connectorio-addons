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

import org.connectorio.addons.link.LinkManager;
import org.openhab.core.common.registry.Registry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This is an alternative channel link manager which rely on different inheritance tree of
 * {@link org.openhab.core.thing.link.ItemChannelLinkRegistry}.
 *
 * Main point of this class is provisioning of {@link org.connectorio.addons.link.LinkManager}
 * implementation which works with pure interfaces.
 */
@Component(service = LinkManager.class)
public class AltChannelLinkManager extends BaseChannelLinkManager {

  @Activate
  public AltChannelLinkManager(@Reference(target = ITEM_CHANNEL_LINK_REGISTRY_FILTER) Registry<ItemChannelLink, String> linkRegistry,
    @Reference ThingRegistry thingRegistry) {
    super(linkRegistry, thingRegistry);
  }

}
