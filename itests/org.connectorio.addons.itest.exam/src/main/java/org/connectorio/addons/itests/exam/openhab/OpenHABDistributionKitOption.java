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
package org.connectorio.addons.itests.exam.openhab;

import org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;

/**
 * Custom option which allows overriding standard Karaf distribution directory layout.
 * It also sets executable and etc/data/log directories.
 */
public class OpenHABDistributionKitOption extends KarafDistributionKitConfigurationOption {

  public OpenHABDistributionKitOption() {
    this(new MavenArtifactUrlReference().groupId("org.openhab.distro").artifactId("openhab").type("zip").versionAsInProject());
  }

  public OpenHABDistributionKitOption(String version) {
    this(new MavenArtifactUrlReference().groupId("org.openhab.distro").artifactId("openhab").type("zip").version(version));
  }

  public OpenHABDistributionKitOption(MavenArtifactUrlReference frameworkUrl) {
    super(frameworkUrl, Platform.NIX);

    karafEtc("../userdata/etc/");
    karafData("../userdata/");
    karafLog("../userdata/log");
  }

  @Override
  public String getExec() {
    return getPlatform() == Platform.NIX ? "../start.sh" : "../start.bat";
  }

}
