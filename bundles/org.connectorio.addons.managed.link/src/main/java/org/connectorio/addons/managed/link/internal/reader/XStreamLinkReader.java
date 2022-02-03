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
package org.connectorio.addons.managed.link.internal.reader;

import com.thoughtworks.xstream.XStream;
import org.connectorio.addons.managed.link.model.BaseLinkEntry;
import org.connectorio.addons.managed.link.model.LinkEntry;
import org.connectorio.addons.managed.link.model.Links;
import org.connectorio.addons.managed.xstream.NestedMapConverter;
import org.openhab.core.config.xml.util.XmlDocumentReader;

public class XStreamLinkReader extends XmlDocumentReader<Links> {

  private XStream xstream;

  public XStreamLinkReader() {
    ClassLoader classLoader = XStreamLinkReader.class.getClassLoader();
    if (classLoader != null) {
      super.setClassLoader(classLoader);
    }
  }

  @Override
  protected void registerConverters(XStream xstream) {
    xstream.registerLocalConverter(BaseLinkEntry.class, "config", new NestedMapConverter());
  }

  @Override
  protected void registerAliases(XStream xstream) {
    xstream.alias("links", Links.class);
    xstream.useAttributeFor(LinkEntry.class, "item");
    xstream.addImplicitCollection(Links.class, "links", "link", LinkEntry.class);
    xstream.alias("link", LinkEntry.class);
  }

  // OH!
  public void configureSecurity(XStream xstream) {
    this.xstream = xstream;
    xstream.allowTypes(new Class[] {
      Links.class, LinkEntry.class
    });
  }

  // OSH!
  public void registerSecurity(XStream xStream) {
    configureSecurity(xStream);
  }


  public String write(Links config) {
    return xstream.toXML(config);
  }
}
