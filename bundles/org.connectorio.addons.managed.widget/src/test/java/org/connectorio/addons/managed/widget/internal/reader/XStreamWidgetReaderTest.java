package org.connectorio.addons.managed.widget.internal.reader;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.connectorio.addons.managed.widget.model.Components;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XStreamWidgetReaderTest {

  @TempDir
  static Path directory;

  @Test
  void testReader() throws Exception {
    XStreamWidgetReader reader = new XStreamWidgetReader();
    Components components = reader.readFromXML(getClass().getResource("/widgets.xml"));

    String value = reader.write(components);

    Path testFile = directory.resolve("test.xml");
    Files.write(testFile, value.getBytes());

    Components deserialized = reader.readFromXML(testFile.toUri().toURL());
    assertThat(deserialized).isEqualTo(components);
  }

}