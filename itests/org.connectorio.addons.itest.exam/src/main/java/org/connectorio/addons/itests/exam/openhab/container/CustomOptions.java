/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.itests.exam.openhab.container;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.TimeoutOption;
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;

public class CustomOptions implements CompositeOption {

  private final String version;

  public CustomOptions() {
    String itestBaseReference = maven("org.connectorio.addons", "org.connectorio.addons.itest.base")
      .versionAsInProject().getURL();
    this.version = itestBaseReference.substring(itestBaseReference.lastIndexOf("/") + 1);
  }

  @Override
  public Option[] getOptions() {
    return new Option[] {
        keepRuntimeFolder(),
        mavenBundle("org.connectorio.addons", "org.connectorio.addons.itest.base").versionAsInProject(),
        new TimeoutOption(60_000),
        new WorkingDirectoryOption("target/karaf"),
        logLevel(LogLevel.DEBUG),
        editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.localRepository",
          new File("target/pax-exam").toPath().toAbsolutePath().toString()),
    };
  }
}
