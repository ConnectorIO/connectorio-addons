package org.connectorio.addons.persistence.manager.internal.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.connectorio.addons.persistence.manager.HasNamePatternPersistenceFilter;
import org.connectorio.addons.persistence.manager.HasTagPersistenceFilter;
import org.junit.jupiter.api.Test;
import org.openhab.core.persistence.PersistenceItemConfiguration;
import org.openhab.core.persistence.config.PersistenceAllConfig;
import org.openhab.core.persistence.config.PersistenceConfig;
import org.openhab.core.persistence.config.PersistenceGroupConfig;
import org.openhab.core.persistence.config.PersistenceItemConfig;
import org.openhab.core.persistence.registry.PersistenceServiceConfiguration;
import org.openhab.core.persistence.strategy.PersistenceCronStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy.Globals;

class PersistenceXmlReaderTest {

  @Test
  void testReader() throws Exception {
    PersistenceXmlReader reader = new PersistenceXmlReader();
    PersistenceServiceConfiguration config = reader.readFromXML(getClass().getResource("/persistence.xml"));

    List<PersistenceConfig> items = Arrays.asList(new PersistenceAllConfig());
    String alias = "";
    List<PersistenceStrategy> itemStrategies = new ArrayList<>();
    itemStrategies.add(Globals.RESTORE);
    itemStrategies.add(Globals.UPDATE);
    List<PersistenceItemConfiguration> configs = new ArrayList<>();
    configs.add(new PersistenceItemConfiguration(items, alias, itemStrategies, Arrays.asList(new HasTagPersistenceFilter("Computed"), new HasNamePatternPersistenceFilter(".*?_([^Current]*)$"))));
    configs.add(new PersistenceItemConfiguration(Arrays.asList(new PersistenceItemConfig("Sample_Item")), "itemCfg", itemStrategies, null));
    configs.add(new PersistenceItemConfiguration(Arrays.asList(new PersistenceGroupConfig("Sample_Group")), "groupCfg", itemStrategies, null));
    List<PersistenceStrategy> defaults = Arrays.asList(Globals.CHANGE, new PersistenceCronStrategy("everyHour", "0 ? ? ?"));
    List<PersistenceStrategy> strategies = new ArrayList<>();
    strategies.add(Globals.CHANGE);
    PersistenceServiceConfiguration configuration = new PersistenceServiceConfiguration("a",
      configs, defaults, strategies, Collections.emptyList()
    );

    assertThat(config.getDefaults()).isEqualTo(configuration.getDefaults());
    assertThat(config.getStrategies()).isEqualTo(configuration.getStrategies());

    List<PersistenceItemConfiguration> configConfigs = config.getConfigs();
    for (int index = 0, size = configConfigs.size(); index < size; index++) {
      PersistenceItemConfiguration itemConfiguration = configConfigs.get(index);
      PersistenceItemConfiguration source = configuration.getConfigs().get(index);

      assertThat(itemConfiguration.alias()).isEqualTo(source.alias());
      // persistence configuration elements do not define equals/hash code!
      assertThat(itemConfiguration.items().toString()).isEqualTo(source.items().toString());
      assertThat(itemConfiguration.strategies()).isEqualTo(source.strategies());
      if (source.filters() != null) {
        assertThat(itemConfiguration.filters()).isEqualTo(source.filters());
      }
    }
  }

}