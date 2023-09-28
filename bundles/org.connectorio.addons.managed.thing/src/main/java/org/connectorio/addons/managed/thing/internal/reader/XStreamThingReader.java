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
package org.connectorio.addons.managed.thing.internal.reader;

import com.thoughtworks.xstream.XStream;
import org.connectorio.addons.managed.thing.model.BridgeEntry;
import org.connectorio.addons.managed.thing.model.ChannelEntry;
import org.connectorio.addons.managed.thing.model.ThingEntry;
import org.connectorio.addons.managed.thing.model.Things;
import org.connectorio.addons.managed.xstream.NestedMapConverter;
import org.openhab.core.config.core.xml.util.XmlDocumentReader;

public class XStreamThingReader extends XmlDocumentReader<Things> {

  private XStream xstream;

  public XStreamThingReader() {
    ClassLoader classLoader = XStreamThingReader.class.getClassLoader();
    if (classLoader != null) {
      super.setClassLoader(classLoader);
    }
  }

  @Override
  protected void registerConverters(XStream xstream) {
    xstream.registerLocalConverter(ThingEntry.class, "config", new NestedMapConverter());
    xstream.registerLocalConverter(ChannelEntry.class, "config", new NestedMapConverter());
  }

  @Override
  protected void registerAliases(XStream xstream) {
    xstream.alias("things",Things.class);
    xstream.addImplicitCollection(Things.class, "things");
    xstream.alias("thing", ThingEntry.class);
    xstream.alias("bridge", BridgeEntry.class);
    xstream.useAttributeFor(ThingEntry.class, "label");
    xstream.useAttributeFor(ThingEntry.class, "type");

    xstream.addImplicitCollection(ThingEntry.class, "channels", "channel", ChannelEntry.class);

    xstream.useAttributeFor(ThingEntry.class, "label");
  }

  // OH!
  public void configureSecurity(XStream xstream) {
    xstream.allowTypes(new Class[] { Things.class, ThingEntry.class, BridgeEntry.class, ChannelEntry.class });
    this.xstream = xstream;
  }

  // OSH!
  public void registerSecurity(XStream xStream) {
    configureSecurity(xStream);
  }


  public String write(Things things) {
    return xstream.toXML(things);
  }

  public XStream getXStream() {
    return xstream;
  }

}
