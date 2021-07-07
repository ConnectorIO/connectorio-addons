package org.connectorio.addons.persistence.manager.internal.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.openhab.core.persistence.config.PersistenceItemConfig;

class PersistenceItemConfigConverter implements Converter {

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    writer.addAttribute("name", ((PersistenceItemConfig) source).getItem());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return new PersistenceItemConfig(reader.getAttribute("name"));
  }

  @Override
  public boolean canConvert(Class type) {
    return PersistenceItemConfig.class.isAssignableFrom(type);
  }
}
