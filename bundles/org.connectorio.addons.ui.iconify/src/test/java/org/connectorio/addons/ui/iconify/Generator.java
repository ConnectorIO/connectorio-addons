package org.connectorio.addons.ui.iconify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import org.connectorio.addons.ui.iconify.descriptor.Collection;
import org.connectorio.addons.ui.iconify.descriptor.IconifyIcon;
import org.connectorio.addons.ui.iconify.descriptor.IconifyIconSet;

public class Generator {

  private final ObjectMapper mapper = new ObjectMapper();
  private final String version;
  private boolean cache;
  private final File iconPath;

  public Generator(String version, boolean cache, String iconPath) {
    this.version = version;
    this.cache = cache;
    this.iconPath = new File(iconPath);
    if (this.iconPath.exists()) {
      this.iconPath.mkdirs();
    }
  }

  public Generator() {
    this("2.2.20", true);
  }

  public Generator(String version, boolean cache) {
    this(version, cache, "target/iconify/svgs");
  }

  private void run() throws Exception {
    URL src = fetch(
      new URL("https://raw.githubusercontent.com/iconify/icon-sets/" + version + "/collections.json"),
      new File("target/iconify/collections.json")
    );
    System.out.println("Reading resources from " + src);

    Map<String, Collection> collections = mapper.readValue(src, new TypeReference<Map<String, Collection>>() {});
    for (Entry<String, Collection> entry : collections.entrySet()) {
      Collection collection = entry.getValue();
      if ("general".equalsIgnoreCase(collection.getCategory()) && "Apache-2.0".equals(collection.getLicense().getSpdx())) {
        String iconSetId = entry.getKey();
        System.out.println(iconSetId + " " + collection.getName());
        URL iconSetUrl = fetch(
          new URL("https://raw.githubusercontent.com/iconify/icon-sets/" + version + "/json/" + iconSetId + ".json"),
          new File("target/iconify/json/" + iconSetId + ".json")
        );
        IconifyIconSet iconSet = mapper.readValue(iconSetUrl, IconifyIconSet.class);
        for (Entry<String, IconifyIcon> icons : iconSet.getIcons().entrySet()) {
          String iconId = icons.getKey();
          IconifyIcon icon = icons.getValue();
          File iconFile = new File(iconPath + "/icons/" + iconSetId + "/" + iconId + ".svg");
          iconFile.getParentFile().mkdirs();
          BufferedWriter writer = new BufferedWriter(new FileWriter(iconFile));
          writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
          writer.write("<!--\n");
          writer.write(String.format(" - Generated from iconify iconset %s\n", iconSetId));
          writer.write(String.format(" - Author %s\n", collection.getAuthor().getName()));
          writer.write(String.format(" - See %s\n", collection.getAuthor().getUrl()));
          writer.write(String.format(" - License: %s, %s\n", collection.getLicense().getTitle(), collection.getLicense().getUrl()));
          writer.write(String.format(" - SPDX-License-Identifier: %s\n", collection.getLicense().getSpdx()));
          writer.write("-->\n");
          writer.write(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1em\" viewBox=\"0 0 %d %d\">\n",
            icon.getWidth() != null ? icon.getWidth() : iconSet.getWidth(),
            iconSet.getHeight()
          ));
          writer.write(icon.getBody() + "\n");
          writer.write("</svg>\n");
          writer.flush();
          writer.close();
        }
      }
    }
  }

  private URL fetch(URL source, File cache) throws IOException {
    if (this.cache) {
      if (!cache.exists()) {
        cache.getParentFile().mkdirs();
        URLConnection connection = source.openConnection();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(new FileWriter(cache, StandardCharsets.UTF_8));
        reader.transferTo(writer);
        writer.flush();
      }
      return cache.toURI().toURL();
    }
    return source;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out.println("Missing required arguments");
      return;
    }
    new Generator(
      args[0],
      Boolean.parseBoolean(args[1]),
      args[2]
    ).run();
  }

}
