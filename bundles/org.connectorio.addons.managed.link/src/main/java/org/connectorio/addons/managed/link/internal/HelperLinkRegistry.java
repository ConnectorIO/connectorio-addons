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

import org.connectorio.addons.managed.link.LinkRegistry;
import org.openhab.core.common.registry.AbstractRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(service = LinkRegistry.class)
public class HelperLinkRegistry extends AbstractRegistry<ItemChannelLink, String, ItemChannelLinkProvider>
  implements LinkRegistry{

  public HelperLinkRegistry() {
    super(ItemChannelLinkProvider.class);
  }

  @Activate
  @Override
  protected void activate(BundleContext context) {
    super.activate(context);
  }

  @Deactivate
  @Override
  protected void deactivate() {
    super.deactivate();
  }

}
