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
package org.connectorio.addons.managed.widget.internal.reader;

import com.thoughtworks.xstream.XStream;
import org.connectorio.addons.managed.widget.model.ComponentEntry;
import org.connectorio.addons.managed.widget.model.Components;
import org.connectorio.addons.managed.widget.model.RootEntry;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.config.core.dto.ConfigDescriptionDTO;
import org.openhab.core.config.core.dto.ConfigDescriptionParameterDTO;
import org.openhab.core.config.core.dto.ConfigDescriptionParameterGroupDTO;
import org.openhab.core.config.xml.util.XmlDocumentReader;

public class XStreamWidgetReader extends XmlDocumentReader<Components> {

  private XStream xstream;

  public XStreamWidgetReader() {
    ClassLoader classLoader = XStreamWidgetReader.class.getClassLoader();
    if (classLoader != null) {
      super.setClassLoader(classLoader);
    }
  }

  @Override
  public void registerConverters(XStream xstream) {
    xstream.registerLocalConverter(RootEntry.class, "config", new NestedMapConverter());
    xstream.registerLocalConverter(RootEntry.class, "slots", new NestedMapConverter());
    xstream.registerLocalConverter(ComponentEntry.class, "config", new NestedMapConverter());
    xstream.registerLocalConverter(ComponentEntry.class, "slots", new NestedMapConverter());
  }

  @Override
  public void registerAliases(XStream xstream) {
    xstream.alias("components", Components.class);
    xstream.addImplicitCollection(Components.class, "components");
    xstream.alias("root", RootEntry.class);
    xstream.alias("component", ComponentEntry.class);

    xstream.useAttributeFor(RootEntry.class, "uid");
    xstream.useAttributeFor(RootEntry.class, "type");

  }

  // OH!
  public void configureSecurity(XStream xstream) {
    xstream.allowTypes(new Class[] { Components.class, RootEntry.class, ComponentEntry.class, ConfigDescriptionDTO.class,
      ConfigDescriptionParameterDTO.class, ConfigDescriptionParameterGroupDTO.class});
    this.xstream = xstream;
  }

  // OSH!
  public void registerSecurity(XStream xStream) {
    configureSecurity(xStream);
  }

  public String write(Components things) {
    return xstream.toXML(things);
  }

  public XStream getXStream() {
    return xstream;
  }

}
