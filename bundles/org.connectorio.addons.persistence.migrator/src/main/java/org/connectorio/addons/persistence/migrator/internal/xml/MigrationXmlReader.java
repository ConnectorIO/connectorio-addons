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
package org.connectorio.addons.persistence.migrator.internal.xml;

import com.thoughtworks.xstream.XStream;
import org.connectorio.addons.persistence.migrator.internal.operation.CopyPatternOperation;
import org.connectorio.addons.persistence.migrator.internal.operation.DeleteOperation;
import org.connectorio.addons.persistence.migrator.internal.operation.CopyOperation;
import org.connectorio.addons.persistence.migrator.internal.operation.TruncateOperation;
import org.openhab.core.config.xml.util.XmlDocumentReader;

public class MigrationXmlReader extends XmlDocumentReader<Migrations> {

  private XStream xstream;

  public MigrationXmlReader() {
    ClassLoader classLoader = MigrationXmlReader.class.getClassLoader();
    if (classLoader != null) {
      super.setClassLoader(classLoader);
    }
  }

  @Override
  protected void registerConverters(XStream xstream) {
  }

  @Override
  protected void registerAliases(XStream xstream) {
    xstream.alias("migrations", Migrations.class);
    xstream.alias("copy", CopyOperation.class);
    xstream.alias("delete", DeleteOperation.class);
    xstream.alias("truncate", TruncateOperation.class);
    xstream.aliasType("source", ItemReference.class);
    xstream.aliasType("target", ItemReference.class);
    xstream.useAttributeFor(Migrations.class, "service");
    xstream.useAttributeFor(ItemReference.class, "item");
    xstream.useAttributeFor(ItemReference.class, "service");

    xstream.addImplicitCollection(Migrations.class, "steps", "copy", CopyOperation.class);
    xstream.addImplicitCollection(Migrations.class, "steps", "copyPattern", CopyPatternOperation.class);
    xstream.addImplicitCollection(Migrations.class, "steps", "delete", DeleteOperation.class);
    xstream.addImplicitCollection(Migrations.class, "steps", "truncate", TruncateOperation.class);
  }

  // OH!
  public void configureSecurity(XStream xstream) {
    xstream.allowTypes(new Class[] { Migrations.class,
    });
    this.xstream = xstream;
  }

  // OSH!
  public void registerSecurity(XStream xStream) {
    configureSecurity(xStream);
  }

}
