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

import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.karaf.container.internal.KarafTestContainer;
import org.ops4j.pax.exam.karaf.container.internal.KarafTestContainerFactory;
import org.ops4j.pax.exam.karaf.container.internal.runner.KarafEmbeddedRunner;
import org.ops4j.pax.exam.karaf.container.internal.runner.KarafJavaRunner;
import org.ops4j.pax.exam.karaf.container.internal.runner.NixRunner;
import org.ops4j.pax.exam.karaf.container.internal.runner.WindowsRunner;
import org.ops4j.pax.exam.karaf.options.KarafDistributionBaseConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for openhab runtime.
 */
public class OpenHABTestContainerFactory extends KarafTestContainerFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(KarafTestContainer.class);
  private static final boolean IS_WINDOWS_OS = System.getProperty("os.name").toLowerCase().contains("windows");

  /**
   * {@inheritDoc}
   */
  @Override
  public TestContainer[] create(ExamSystem system) {
    List<TestContainer> containers = new ArrayList<TestContainer>();
    KarafDistributionKitConfigurationOption[] kitOptions =
        system.getOptions(KarafDistributionKitConfigurationOption.class);
    for (KarafDistributionKitConfigurationOption kitOption : kitOptions) {
      if (kitOption.getPlatform().equals(Platform.WINDOWS)) {
        if (IS_WINDOWS_OS) {
          containers.add(new OpenHABTestContainer(system, kitOption, new WindowsRunner(kitOption.getMakeExec(), kitOption.getExec())));
          continue;
        }
        LOGGER.info("Ignore windows settings on non windows platforms");
      }
      else {
        if (!IS_WINDOWS_OS) {
          containers.add(new OpenHABTestContainer(system, kitOption, new NixRunner(kitOption.getMakeExec(), kitOption.getExec())));
          continue;
        }
        LOGGER.info("Ignore non windows settings on windows platforms");
      }
    }

    KarafDistributionBaseConfigurationOption[] options = system.getOptions(KarafDistributionConfigurationOption.class);
    for (KarafDistributionBaseConfigurationOption testContainer : options) {
      if (testContainer.isRunEmbedded()) {
        containers.add(new OpenHABTestContainer(system, testContainer, new KarafEmbeddedRunner()));
      }
      else {
        containers.add(new OpenHABTestContainer(system, testContainer, new KarafJavaRunner()));
      }
    }
    return containers.toArray(new TestContainer[containers.size()]);
  }


}
