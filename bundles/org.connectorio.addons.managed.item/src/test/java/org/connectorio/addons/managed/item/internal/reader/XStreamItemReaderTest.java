package org.connectorio.addons.managed.item.internal.reader;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.connectorio.addons.managed.item.model.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XStreamItemReaderTest {

  @TempDir
  static Path directory;

  @Test
  void testReader() throws Exception {
    XStreamItemReader reader = new XStreamItemReader();
    Items items = reader.readFromXML(getClass().getResource("/items.xml"));

    String value = reader.write(items);

    Path testFile = directory.resolve("test.xml");
    Files.write(testFile, value.getBytes());

    Items deserialized = reader.readFromXML(testFile.toUri().toURL());
    assertThat(deserialized).isEqualTo(items);
  }

}