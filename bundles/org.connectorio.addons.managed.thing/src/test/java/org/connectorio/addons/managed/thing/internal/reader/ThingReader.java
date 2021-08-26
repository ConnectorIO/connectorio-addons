package org.connectorio.addons.managed.thing.internal.reader;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.managed.thing.model.BridgeEntry;
import org.connectorio.addons.managed.thing.model.ChannelEntry;
import org.connectorio.addons.managed.thing.model.ThingEntry;
import org.connectorio.addons.managed.thing.model.Things;
import org.junit.jupiter.api.Test;

class ThingReader {

  @Test
  void testReader() throws Exception {
    XStreamThingReader reader = new XStreamThingReader();
    Things config = reader.readFromXML(getClass().getResource("/things.xml"));

    System.out.println(config);

    for (ThingEntry item : config.getThings()) {
      if (item instanceof BridgeEntry) {
        System.out.print(" (Bridge) ");
      };
      System.out.print(item.getType() + " ");
      System.out.println(item.getId());
      System.out.println(" Label: " + item.getLabel());
      if (item.getConfig() != null && !item.getConfig().isEmpty()) {
        System.out.println(" Config:");
        printRecursive(item.getConfig(), 1, "  ");
      }
      if (item.getChannels() != null && !item.getChannels().isEmpty()) {
        System.out.println(" Channels:");
        for (ChannelEntry channelEntry : item.getChannels()) {
          System.out.println("  - " + channelEntry.getId() + " " + channelEntry.getLabel());
          if (channelEntry.getConfig() != null) {
            System.out.println("   Config:");
            printRecursive(channelEntry.getConfig(), 2, "  ");
          }
        }
      }
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