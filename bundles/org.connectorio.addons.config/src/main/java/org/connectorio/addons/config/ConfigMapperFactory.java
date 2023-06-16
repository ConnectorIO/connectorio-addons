package org.connectorio.addons.config;

public interface ConfigMapperFactory {

  <T> ConfigMapper<T> createMapper(Class<T> type);

}
