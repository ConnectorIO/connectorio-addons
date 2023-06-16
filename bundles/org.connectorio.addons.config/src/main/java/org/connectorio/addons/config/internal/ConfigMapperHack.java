package org.connectorio.addons.config.internal;

import java.util.Map;
import org.connectorio.addons.config.ConfigMapper;
import org.openhab.core.config.core.Configuration;

public class ConfigMapperHack<T> implements ConfigMapper<T> {

  private final Class<T> type;

  public ConfigMapperHack(Class<T> type) {
    this.type = type;
  }

  @Override
  public T map(Map<String, Object> configuration) {
    return org.openhab.core.config.core.internal.ConfigMapper.as(configuration, type);
  }

  @Override
  public T map(Configuration configuration) {
    return map(configuration.getProperties());
  }

}
