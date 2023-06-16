package org.connectorio.addons.config;

import java.util.Map;
import org.openhab.core.config.core.Configuration;

public interface ConfigMapper<T> {

  T map(Map<String, Object> configuration);

  T map(Configuration configuration);

}
