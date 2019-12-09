package org.connectorio.binding.test;

import static org.mockito.Mockito.when;

import org.eclipse.smarthome.config.core.Configuration;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class ConfigurationMock<T extends org.connectorio.binding.base.config.Configuration> {

  private final Configuration configuration = Mockito.mock(Configuration.class);

  public Configuration get(T mapped) {
    when(configuration.as(mapped.getClass())).thenAnswer((Answer<Object>) invocation -> mapped);
    return configuration;
  }

}
