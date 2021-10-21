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
package org.connectorio.addons.managed.widget.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.connectorio.addons.managed.widget.internal.reader.XStreamWidgetReader;
import org.connectorio.addons.managed.widget.model.ComponentEntry;
import org.connectorio.addons.managed.widget.model.Components;
import org.connectorio.addons.managed.widget.model.RootEntry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.ui.components.RootUIComponent;
import org.openhab.core.ui.components.UIComponent;
import org.openhab.core.ui.components.UIComponentRegistry;
import org.openhab.core.ui.components.UIComponentRegistryFactory;
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
  private final UIComponentRegistryFactory registryFactory;

  @Activate
  public ExportServlet(@Reference HttpService service, @Reference UIComponentRegistryFactory registryFactory) throws ServletException, NamespaceException {
    this.service = service;
    this.registryFactory = registryFactory;
    service.registerServlet("/manage/export/widget", this, new Hashtable<>(), null);
  }

  @Deactivate
  public void deactivate() {
    try {
      service.unregister("/manage/export/widget");
    } catch (IllegalArgumentException e) {
      logger.debug("Failed to unregister export servlet, did it fail while registering?", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("Content-Type", "text/xml; charset=UTF-8");

    String namespace = req.getParameter("namespace");
    if (namespace == null) {
      return;
    }

    UIComponentRegistry registry = registryFactory.getRegistry(namespace);
    List<RootEntry> roots = new ArrayList<>();
    for (RootUIComponent root : registry.getAll()) {
      RootEntry rootEntry = new RootEntry();

      rootEntry.setUid(root.getUID());
      rootEntry.setTags(root.getTags());
      rootEntry.setProps(root.getProps());
      rootEntry.setTimestamp(root.getTimestamp());

      mapComponent(root, rootEntry);
      roots.add(rootEntry);
    }

    XStreamWidgetReader reader = new XStreamWidgetReader();
    resp.getWriter().write(reader.write(new Components(roots)));
  }

  void mapComponent(UIComponent component, ComponentEntry dto) {
    dto.setConfig(component.getConfig());
    dto.setType(component.getType());
    Map<String, List<ComponentEntry>> slots = new LinkedHashMap<>();
    dto.setSlots(slots);

    if (component.getSlots() != null) {
      for (Map.Entry<String, List<UIComponent>> entry : component.getSlots().entrySet()) {
        List<ComponentEntry> subComponents = new ArrayList<>();
        for (UIComponent subComponent : entry.getValue()) {
          ComponentEntry componentEntry = new ComponentEntry();
          mapComponent(subComponent, componentEntry);
          subComponents.add(componentEntry);
        }

        slots.put(entry.getKey(), subComponents);
      }
    }
  }

}
