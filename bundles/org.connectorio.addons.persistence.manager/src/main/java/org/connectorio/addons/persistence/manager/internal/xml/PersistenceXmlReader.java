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
package org.connectorio.addons.persistence.manager.internal.xml;

import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.connectorio.addons.persistence.manager.HasNamePatternPersistenceFilter;
import org.connectorio.addons.persistence.manager.HasTagPersistenceFilter;
import org.openhab.core.config.core.xml.util.XmlDocumentReader;
import org.openhab.core.persistence.PersistenceItemConfiguration;
import org.openhab.core.persistence.filter.PersistenceFilter;
import org.openhab.core.persistence.registry.PersistenceServiceConfiguration;
import org.openhab.core.persistence.config.PersistenceAllConfig;
import org.openhab.core.persistence.config.PersistenceConfig;
import org.openhab.core.persistence.config.PersistenceGroupConfig;
import org.openhab.core.persistence.config.PersistenceItemConfig;
import org.openhab.core.persistence.strategy.PersistenceCronStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy;

public class PersistenceXmlReader extends XmlDocumentReader<PersistenceServiceConfiguration> {

  private XStream xstream;

  public PersistenceXmlReader() {
    ClassLoader classLoader = PersistenceXmlReader.class.getClassLoader();
    if (classLoader != null) {
      super.setClassLoader(classLoader);
    }
  }

  @Override
  protected void registerConverters(XStream xstream) {
    xstream.registerConverter(new PersistenceItemConfigConverter());
    xstream.registerConverter(new PersistenceGroupConfigConverter());
    //xstream.registerConverter(new PersistenceAllConfigConverter());
    xstream.registerConverter(new PersistenceItemConfigurationConverter());
    xstream.registerConverter(new PersistenceStrategyConverter());
    xstream.registerConverter(new HasNamePatternPersistenceFilterConverter());

    xstream.addDefaultImplementation(MutablePersistenceServiceConfiguration.class, PersistenceServiceConfiguration.class);
  }

  @Override
  protected void registerAliases(XStream xstream) {
    xstream.alias("all", PersistenceAllConfig.class);
    xstream.alias("group", PersistenceGroupConfig.class);
    xstream.alias("item", PersistenceItemConfig.class);
    xstream.alias("hasTag", HasTagPersistenceFilter.class);
    xstream.useAttributeFor(HasTagPersistenceFilter.class, "name");
    xstream.alias("hasName", HasNamePatternPersistenceFilter.class);
    xstream.useAttributeFor(HasNamePatternPersistenceFilter.class, "pattern");

    xstream.alias("service", MutablePersistenceServiceConfiguration.class);
    xstream.addImplicitCollection(MutablePersistenceServiceConfiguration.class, "configs");

    xstream.alias("config", PersistenceItemConfiguration.class);
    xstream.alias("strategy", PersistenceStrategy.class);
    xstream.alias("cron", PersistenceCronStrategy.class);
  }

  // OH!
  public void configureSecurity(XStream xstream) {
    xstream.allowTypes(new Class[] { MutablePersistenceServiceConfiguration.class,
      PersistenceItemConfiguration.class, PersistenceStrategy.class, PersistenceCronStrategy.class,
      PersistenceAllConfig.class, PersistenceGroupConfig.class, PersistenceItemConfig.class,
      HasTagPersistenceFilter.class, HasNamePatternPersistenceFilter.class,
      MutablePersistenceServiceConfiguration.class, PersistenceItemConfiguration.class
    });
    this.xstream = xstream;
  }

  // OSH!
  public void registerSecurity(XStream xStream) {
    configureSecurity(xStream);
  }


  public static class MutablePersistenceServiceConfiguration extends PersistenceServiceConfiguration {
    private List<PersistenceItemConfiguration> configs = new ArrayList<>();
    private List<PersistenceStrategy> defaults = new ArrayList<>();
    private List<PersistenceStrategy> strategies = new ArrayList<>();
    private List<PersistenceFilter> filters = new ArrayList<>();

    public MutablePersistenceServiceConfiguration(String serviceId) {
      super(serviceId, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public List<PersistenceItemConfiguration> getConfigs() {
      return configs;
    }

    @Override
    public List<PersistenceStrategy> getDefaults() {
      return defaults;
    }

    @Override
    public List<PersistenceStrategy> getStrategies() {
      return strategies;
    }

    @Override
    public List<PersistenceFilter> getFilters() {
      return filters;
    }

    public void setConfigs(List<PersistenceItemConfiguration> configs) {
      this.configs = configs;
    }

    public void setDefaults(List<PersistenceStrategy> defaults) {
      this.defaults = defaults;
    }

    public void setStrategies(List<PersistenceStrategy> strategies) {
      this.strategies = strategies;
    }

    public void setFilters(List<PersistenceFilter> filters) {
      this.filters = filters;
    }

    @Override
    public String toString() {
      return "MutablePersistenceServiceConfiguration[configs=" + getConfigs()
        + ", defaults=" + getDefaults()
          + ", strategies=" + getStrategies()
          + ", filters=" + getFilters()
        + "]";
    }
  }

}
