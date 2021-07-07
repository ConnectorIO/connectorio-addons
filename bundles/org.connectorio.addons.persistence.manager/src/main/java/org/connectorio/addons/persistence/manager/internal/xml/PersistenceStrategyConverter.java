package org.connectorio.addons.persistence.manager.internal.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.connectorio.addons.persistence.manager.internal.PersistenceStrategies;
import org.openhab.core.persistence.strategy.PersistenceCronStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy.Globals;

class PersistenceStrategyConverter implements Converter {

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    PersistenceStrategy strategy = (PersistenceStrategy) source;
    writer.addAttribute("name", strategy.getName());
    if (source instanceof PersistenceCronStrategy) {
      writer.setValue(((PersistenceCronStrategy) source).getCronExpression());
    }
  }

  @Override
  public boolean canConvert(Class type) {
    return PersistenceStrategy.class.isAssignableFrom(type);
  }

  @Override
  public PersistenceStrategy unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String name = reader.getAttribute("name");
    if (Globals.CHANGE.getName().equalsIgnoreCase(name)) {
      return Globals.CHANGE;
    }
    if (Globals.RESTORE.getName().equalsIgnoreCase(name)) {
      return Globals.RESTORE;
    }
    if (Globals.UPDATE.getName().equalsIgnoreCase(name)) {
      return Globals.UPDATE;
    }
    if (PersistenceStrategies.NONE.getName().equalsIgnoreCase(name)) {
      return PersistenceStrategies.NONE;
    }
    return new PersistenceCronStrategy(name, reader.getValue());
  }
}
