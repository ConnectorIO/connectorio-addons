package org.connectorio.addons.config.internal;

import org.connectorio.addons.config.ConfigMapper;
import org.connectorio.addons.config.ConfigMapperFactory;
import org.osgi.service.component.annotations.Component;

@Component(service = ConfigMapperFactory.class)
public class DefaultConfigMapperFactory implements ConfigMapperFactory {

  @Override
  public <T> ConfigMapper<T> createMapper(Class<T> type) {
    return new ConfigMapperHack<>(type);
  }
}
