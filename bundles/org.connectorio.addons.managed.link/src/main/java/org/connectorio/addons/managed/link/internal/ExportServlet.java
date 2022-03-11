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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.connectorio.addons.managed.link.LinkRegistry;
import org.connectorio.addons.managed.link.internal.reader.XStreamLinkReader;
import org.connectorio.addons.managed.link.model.LinkEntry;
import org.connectorio.addons.managed.link.model.Links;
import org.openhab.core.thing.link.AbstractLink;
import org.openhab.core.thing.link.ItemChannelLink;
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
  private final LinkRegistry linkRegistry;

  @Activate
  public ExportServlet(@Reference HttpService service, @Reference LinkRegistry linkRegistry) throws ServletException, NamespaceException {
    this.service = service;
    this.linkRegistry = linkRegistry;
    service.registerServlet("/manage/export/link", this, new Hashtable<>(), null);
  }

  @Deactivate
  public void deactivate() {
    try {
      service.unregister("/manage/export/link");
    } catch (IllegalArgumentException e) {
      logger.debug("Failed to unregister export servlet, did it fail while registering?", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("Content-Type", "text/xml; charset=UTF-8");

    List<LinkEntry> items = new ArrayList<>();
    for (ItemChannelLink link : sorted()) {
      LinkEntry entry = new LinkEntry();
      items.add(entry);
      entry.setChannel(link.getLinkedUID().getAsString());
      entry.setConfig(link.getConfiguration().getProperties());
      entry.setItem(link.getItemName());
    }

    XStreamLinkReader reader = new XStreamLinkReader();
    resp.getWriter().write(reader.write(new Links(items)));

  }

  private Collection<ItemChannelLink> sorted() {
    Set<ItemChannelLink> sorted = new TreeSet<>(Comparator.comparing(AbstractLink::getItemName).thenComparing(AbstractLink::getUID));
    sorted.addAll(linkRegistry.getAll());
    return sorted;
  }

}
