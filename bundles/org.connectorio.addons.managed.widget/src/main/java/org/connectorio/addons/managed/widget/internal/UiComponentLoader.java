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

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.managed.widget.internal.reader.XStreamWidgetReader;
import org.connectorio.addons.managed.widget.model.ComponentEntry;
import org.connectorio.addons.managed.widget.model.Components;
import org.connectorio.addons.managed.widget.model.RootEntry;
import org.openhab.core.ui.components.RootUIComponent;
import org.openhab.core.ui.components.UIComponent;
import org.openhab.core.ui.components.UIProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UiComponentLoader {

  private final Logger logger = LoggerFactory.getLogger(UiComponentLoader.class);
  private final BundleContext context;

  List<ServiceRegistration<?>> registrations = new ArrayList<>();

  @Activate
  public UiComponentLoader(BundleContext context) {
    this.context = context;
    File managed = new File(System.getProperty("openhab.userdata"), "managed");
    logger.info("Attempting to load link definitions from files located in {}", managed);
    if (!managed.isDirectory() && managed.exists()) {
      logger.trace("Directory {} not found, attempting to create", managed);
      managed.mkdirs();
    }

    File[] files = managed.listFiles(f -> f.getName().contains("ui-widget") || f.getName().contains("ui-page"));
    if (files == null) {
      logger.info("No files matching *ui-widget* or *ui-page* name found in {}", managed);
      return;
    }

    List<RootUIComponent> pages = new ArrayList<>();
    List<RootUIComponent> widgets = new ArrayList<>();
    for (File file : files) {
      XStreamWidgetReader reader = new XStreamWidgetReader();
      Components parsedItems;
      try {
        parsedItems = reader.readFromXML(file.toURI().toURL());
        logger.info("Successfully read {} components from {}", parsedItems == null ? 0 : parsedItems.getComponents().size(), file);
      } catch (Exception e) {
        logger.error("Failed to parse document {}", file, e);
        continue;
      }

      for (RootEntry entry : parsedItems.getComponents()) {
        if (file.getName().contains("ui-page")) {
          pages.add(creatComponent(entry));
        }
        if (file.getName().contains("ui-widget")) {
          widgets.add(creatComponent(entry));
        }
      }
    }

    registrations.add(context.registerService(UIProvider.class, new StaticUIProvider("ui:page", pages), new Hashtable<>()));
    registrations.add(context.registerService(UIProvider.class, new StaticUIProvider("ui:widget", widgets), new Hashtable<>()));
  }

  private RootUIComponent creatComponent(RootEntry entry) {
    RootUIComponent component = new RootUIComponent(entry.getUid(), entry.getType());
    component.setProps(entry.getProps());
    component.setTimestamp(entry.getTimestamp());
    if (entry.getTags() != null) {
      component.getTags().addAll(entry.getTags());
    }

    mapComponent(component, entry);

    return component;
  }

  private UIComponent mapComponent(UIComponent component, ComponentEntry element) {
    if (element.getConfig() != null) {
      for (Entry<String, Object> cfg : element.getConfig().entrySet()) {
        component.addConfig(cfg.getKey(), cfg.getValue());
      }
    }
    if (element.getSlots() != null) {
      for (Map.Entry<String, List<ComponentEntry>> entry : element.getSlots().entrySet()) {
        for (ComponentEntry compo : entry.getValue()) {
          UIComponent uiComponent = new UIComponent(compo.getType());
          component.addComponent(entry.getKey(), mapComponent(uiComponent, compo));
        }
      }
    }
    return component;
  }

  @Deactivate
  void deactivate() {
    for (ServiceRegistration<?> registration : registrations) {
      registration.unregister();
    }
  }

}
