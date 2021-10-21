package org.connectorio.addons.managed.widget.internal.reader;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.managed.widget.model.Components;
import org.connectorio.addons.managed.widget.model.RootEntry;
import org.junit.jupiter.api.Test;

class ComponentReader {

  @Test
  void testReader() throws Exception {
    XStreamWidgetReader reader = new XStreamWidgetReader();
    Components config = reader.readFromXML(getClass().getResource("/widgets.xml"));

    System.out.println(config);

    for (RootEntry item : config.getComponents()) {
      System.out.print(item.getUID() + " ");
    }

    System.out.println(reader.write(config));
  }

  private void printRecursive(Map<String, Object> map, int nesting, String prefix) {
    String header = repeat(prefix, nesting);
    for (Entry<String, Object> entry : map.entrySet()) {
      System.out.print(header + entry.getKey());
      if (entry.getValue() instanceof Map) {
        System.out.println();
        printRecursive((Map<String, Object>) entry.getValue(), nesting + 1, prefix);
      } else {
        System.out.println(" = " + entry.getValue());
      }
    }
  }

  private String repeat(String prefix, int nesting) {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < nesting; i++) {
      out.append(prefix);
    }
    return out.toString();
  }

  private void list(Collection<String> groups, String title, String prefix) {
    if (groups != null && !groups.isEmpty()) {
      System.out.println(title);
      for (String group : groups) {
        System.out.println(prefix + group);
      }
    }
  }

}