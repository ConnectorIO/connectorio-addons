package org.connectorio.addons.persistence.manager.internal.xml;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import java.util.List;
import org.openhab.core.config.core.xml.util.GenericUnmarshaller;
import org.openhab.core.persistence.PersistenceItemConfiguration;
import org.openhab.core.persistence.config.PersistenceConfig;
import org.openhab.core.persistence.filter.PersistenceFilter;
import org.openhab.core.persistence.strategy.PersistenceStrategy;

public class PersistenceItemConfigurationConverter extends GenericUnmarshaller<PersistenceItemConfiguration> {

  public PersistenceItemConfigurationConverter() {
    super(PersistenceItemConfiguration.class);
  }

  @Override
  public PersistenceItemConfiguration unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String alias = reader.getAttribute("alias");
    List<PersistenceConfig> items = null;
    List<PersistenceStrategy> strategies = null;
    List<PersistenceFilter> filters = null;

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      if ("items".equals(reader.getNodeName())) {
        items = (List<PersistenceConfig>) context.convertAnother(null, List.class);
      } else if ("strategies".equals(reader.getNodeName())) {
        strategies = (List<PersistenceStrategy>) context.convertAnother(null, List.class);
      } else if ("filters".equals(reader.getNodeName())) {
        filters = (List<PersistenceFilter>) context.convertAnother(null, List.class);
      }
      reader.moveUp();
    }

    return new PersistenceItemConfiguration(
      items, strategies, filters
    );
  }
}
