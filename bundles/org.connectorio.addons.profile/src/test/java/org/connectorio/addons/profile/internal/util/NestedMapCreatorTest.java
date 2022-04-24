package org.connectorio.addons.profile.internal.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.ConfigUtil;

public class NestedMapCreatorTest {

  @Test
  public void testNestedCollection() {
    Map<String, Object> cfg = new LinkedHashMap<>();
    cfg.put("a1.profile", "system:default");
    cfg.put("a1.param1", "value1");
    cfg.put("a1.param2", "value2");
    cfg.put("a2.profile", "system:debounce");
    cfg.put("a2.paramX", Arrays.asList("foo", "bar"));

    // make sure above map fly over key/value validation
    ConfigUtil.normalizeTypes(cfg);

    Map<String, Object> nested = new NestedMapCreator().toNestedMap(cfg);

    Object cfgA1 = nested.get("a1");
    assertThat(cfgA1).isNotNull()
      .isInstanceOf(Map.class);

    assertThat(((Map) cfgA1).get("param1")).isNotNull()
      .isEqualTo("value1");
  }

  @Test
  public void testSortedCollection() {
    Map<String, Object> cfg = new LinkedHashMap<>();
    cfg.put("b1.profile", "system:default");
    cfg.put("b1.param1", "value1");
    cfg.put("a2.profile", "system:debounce");
    cfg.put("a2.paramX", Arrays.asList("foo", "bar"));

    // make sure above map fly over key/value validation
    ConfigUtil.normalizeTypes(cfg);
    Map<String, Object> nested = new NestedMapCreator().toNestedMap(cfg);
    Set<String> keySet = nested.keySet();
    assertThat(keySet).containsExactlyInAnyOrder("a2", "b1");

    Object cfgA2 = nested.get("a2");
    assertThat(cfgA2).isNotNull()
      .isInstanceOf(Map.class);

    assertThat(((Map) cfgA2).get("paramX")).isNotNull();
  }
}
