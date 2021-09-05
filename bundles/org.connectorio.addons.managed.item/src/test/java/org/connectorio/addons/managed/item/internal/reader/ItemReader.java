package org.connectorio.addons.managed.item.internal.reader;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.managed.item.model.GroupEntry;
import org.connectorio.addons.managed.item.model.ItemEntry;
import org.connectorio.addons.managed.item.model.Items;
import org.connectorio.addons.managed.item.model.MetadataEntry;
import org.connectorio.addons.managed.link.model.BaseLinkEntry;

class ItemReader {

  public static void main(String[] args) throws Exception {
    XStreamItemReader reader = new XStreamItemReader();
    Items config = reader.readFromXML(ItemReader.class.getResource("/items.xml"));

    System.out.println(config);

    for (ItemEntry item : config.getItems()) {
      System.out.print(item.getName());
      if (item instanceof GroupEntry) {
        System.out.println(" (group)");
        GroupEntry group = (GroupEntry) item;
        list(group.getMembers(), " Members:", " - ");
      } else {
        System.out.println();
        System.out.println("      Type: " + item.getType());
        System.out.println("     Label: " + item.getLabel());
        System.out.println("  Category: " + item.getCategory());
      }

      list(item.getTags(), " Tags:", " - ");
      list(item.getGroups(), " Groups:", " - ");
      if (item.getMetadata() != null && !item.getMetadata().isEmpty()) {
        System.out.println(" Metadata:");
        for (Entry<String, MetadataEntry> entry : item.getMetadata().entrySet()) {
          System.out.println("  namespace: " + entry.getKey());
          if (entry.getValue().getValue() != null) {
            System.out.println("  value=" + entry.getValue().getValue());
          }
          printRecursive(entry.getValue().getConfig(), 2, "  ");
        }
      }

      if (item.getChannels() != null && !item.getChannels().isEmpty()) {
        System.out.println(" Channels:");
        for (BaseLinkEntry entry : item.getChannels()) {
          System.out.println("  - " + entry.getChannel() + (entry.getConfig() != null ? " " + entry.getConfig() : ""));
        }
      }
    }

    System.out.println(reader.write(config));
  }

  private static void printRecursive(Map<String, Object> map, int nesting, String prefix) {
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

  private static String repeat(String prefix, int nesting) {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < nesting; i++) {
      out.append(prefix);
    }
    return out.toString();
  }

  private static void list(Collection<String> groups, String title, String prefix) {
    if (groups != null && !groups.isEmpty()) {
      System.out.println(title);
      for (String group : groups) {
        System.out.println(prefix + group);
      }
    }
  }

}