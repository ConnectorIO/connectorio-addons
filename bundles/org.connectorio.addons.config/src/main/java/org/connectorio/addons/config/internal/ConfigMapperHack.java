package org.connectorio.addons.config.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.config.ConfigMapper;
import org.openhab.core.config.core.Configuration;

public class ConfigMapperHack<T> implements ConfigMapper<T> {

  private final Class<T> type;

  public ConfigMapperHack(Class<T> type) {
    this.type = type;
  }

  @Override
  public T map(Map<String, Object> configuration) {
    return org.openhab.core.config.core.internal.ConfigMapper.as(cleanup(configuration), type);
  }

  @Override
  public T map(Configuration configuration) {
    return map(configuration.getProperties());
  }

  /**
   * Regular config mapper available in OH core can not handle arrays.
   * This cleanup method transform array entries into list to avoid failures.
    */
  private static Map<String, Object> cleanup(Map<String, Object> configuration) {
    Map<String, Object> config = new HashMap<>();
    for (Entry<String, Object> entry : configuration.entrySet()) {
      if (entry.getValue().getClass().isArray()) {
        config.put(entry.getKey(), list(entry.getValue()));
      } else {
        config.put(entry.getKey(), entry.getValue());
      }
    }
    return config;
  }

  private static List<Object> list(Object value) {
    int length = Array.getLength(value);
    List<Object> list = new ArrayList<>(length);
    for (int index = 0; index < length; index++) {
      list.add(Array.get(value, index));
    }
    return list;
  }

}
