package org.connectorio.addons.persistence.manager.internal.xml;

import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openhab.core.config.xml.util.XmlDocumentReader;
import org.openhab.core.persistence.PersistenceFilter;
import org.openhab.core.persistence.PersistenceItemConfiguration;
import org.openhab.core.persistence.PersistenceServiceConfiguration;
import org.openhab.core.persistence.config.PersistenceAllConfig;
import org.openhab.core.persistence.config.PersistenceConfig;
import org.openhab.core.persistence.config.PersistenceGroupConfig;
import org.openhab.core.persistence.config.PersistenceItemConfig;
import org.openhab.core.persistence.strategy.PersistenceCronStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy;

public class PersistenceXmlReader extends XmlDocumentReader<PersistenceServiceConfiguration> {

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
    xstream.registerConverter(new PersistenceStrategyConverter());

    xstream.addDefaultImplementation(MutablePersistenceServiceConfiguration.class, PersistenceServiceConfiguration.class);
  }

  @Override
  protected void registerAliases(XStream xstream) {
    xstream.alias("all", PersistenceAllConfig.class);
    xstream.alias("group", PersistenceGroupConfig.class);
    xstream.alias("item", PersistenceItemConfig.class);

    xstream.alias("service", MutablePersistenceServiceConfiguration.class);
    xstream.addImplicitCollection(MutablePersistenceServiceConfiguration.class, "configs");

    xstream.alias("config", PersistenceItemConfiguration.class, NullSafePersistenceItemConfiguration.class);
    xstream.useAttributeFor(PersistenceItemConfiguration.class, "alias");
    xstream.alias("strategy", PersistenceStrategy.class);
    xstream.alias("cron", PersistenceCronStrategy.class);
  }

  // OH!
  public void configureSecurity(XStream xstream) {
    xstream.allowTypes(new Class[] { MutablePersistenceServiceConfiguration.class,
      PersistenceItemConfiguration.class, PersistenceStrategy.class, PersistenceCronStrategy.class,
    });
  }

  // OSH!
  public void registerSecurity(XStream xStream) {
    configureSecurity(xStream);
  }

  static class NullSafePersistenceItemConfiguration extends PersistenceItemConfiguration {

    public NullSafePersistenceItemConfiguration(List<PersistenceConfig> items, String alias,
      List<PersistenceStrategy> strategies, List<PersistenceFilter> filters) {
      super(items, alias, strategies, filters == null ? new ArrayList<>() : filters);
    }

    @Override
    public List<PersistenceFilter> getFilters() {
      List<PersistenceFilter> filters = super.getFilters();
      return filters == null ? Collections.emptyList() : filters;
    }
  }

  public static class MutablePersistenceServiceConfiguration extends PersistenceServiceConfiguration {
    private List<PersistenceItemConfiguration> configs = new ArrayList<>();
    private List<PersistenceStrategy> defaults = new ArrayList<>();
    private List<PersistenceStrategy> strategies = new ArrayList<>();

    public MutablePersistenceServiceConfiguration() {
      super(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
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

    public void setConfigs(List<PersistenceItemConfiguration> configs) {
      this.configs = configs;
    }

    public void setDefaults(List<PersistenceStrategy> defaults) {
      this.defaults = defaults;
    }

    public void setStrategies(List<PersistenceStrategy> strategies) {
      this.strategies = strategies;
    }

    @Override
    public String toString() {
      return "MutablePersistenceServiceConfiguration[configs=" + getConfigs()
        + ", defaults=" + getDefaults()
        + ", strategies=" + getStrategies()
        + "]";
    }
  }

}
