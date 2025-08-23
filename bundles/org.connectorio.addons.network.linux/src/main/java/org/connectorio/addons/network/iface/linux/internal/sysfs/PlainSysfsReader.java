/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.network.iface.linux.internal.sysfs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.connectorio.addons.network.iface.linux.internal.SysfsReader;
import org.osgi.service.component.annotations.Component;

@Component
public class PlainSysfsReader implements SysfsReader {

  private final File base;

  public PlainSysfsReader() {
    this("/sys");
  }

  public PlainSysfsReader(String base) {
    this(new File(base));
  }

  public PlainSysfsReader(File base) {
    this.base = base;
  }

  @Override
  public List<String> read(String file) {
    File path = new File(base, file);
    if (path.isFile() && path.canRead()) {
      try {
        return Files.readAllLines(path.toPath());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return Collections.emptyList();
  }

  @Override
  public Set<String> list() {
    if (base.isDirectory() && base.canRead()) {
      Set<String> filesOrDirectories = new LinkedHashSet<>();

      String[] list = base.list();
      if (list == null) {
        return filesOrDirectories;
      }

      filesOrDirectories.addAll(Arrays.asList(list));

      return filesOrDirectories;
    }
    return Collections.emptySet();
  }

  @Override
  public SysfsReader narrow(String ... path) {
    return new PlainSysfsReader(new File(base, Arrays.stream(path)
      .reduce((upper, lower) -> upper + "/" + lower)
      .orElse("")
    ));
  }


}
