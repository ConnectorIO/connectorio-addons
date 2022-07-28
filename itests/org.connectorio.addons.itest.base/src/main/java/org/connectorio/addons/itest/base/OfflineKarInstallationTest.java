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
package org.connectorio.addons.itest.base;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.net.URI;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.kar.KarService;
import org.junit.Test;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.openhab.core.service.StartLevelService;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OfflineKarInstallationTest {

  private final static EnumSet<FeaturesService.Option> INSTALL_OPTIONS = EnumSet.of(FeaturesService.Option.Verbose, FeaturesService.Option.NoAutoRefreshBundles);

  public static final String TEST_KAR = "TEST_KAR";

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final String groupId;
  private final String artifactId;
  private final String featureName;

  @Inject
  protected FeaturesService features;

  @Inject
  protected KarService karService;

  @Inject
  protected ReadyService readyService;

  protected OfflineKarInstallationTest(String groupId, String artifactId, String featureName) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.featureName = featureName;
  }

  protected abstract Set<Option> customize();

  @Configuration
  public Option[] config() {
    MavenArtifactUrlReference urlReference = maven(groupId, artifactId).type("kar").versionAsInProject();

    Set<Option> options = new LinkedHashSet<>(customize());
    options.add(systemProperty(TEST_KAR).value(urlReference.getURL()));

    return options.toArray(new Option[options.size()]);
  }

  @Test
  public void testOfflineFeatureInstallation() throws Exception {
    CountDownLatch lock = new CountDownLatch(1);
    readyService.registerTracker(new ReadyTracker() {
      @Override
      public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        lock.countDown();
      }

      @Override
      public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
      }
    }, new ReadyMarkerFilter().withType(StartLevelService.STARTLEVEL_MARKER_TYPE).withIdentifier("" + StartLevelService.STARTLEVEL_THINGS));

    String location = System.getProperty(TEST_KAR);
    logger.info("Installing kar file {}", location);
    karService.install(URI.create(location));

    lock.await(1, TimeUnit.MINUTES);

    long startup = System.currentTimeMillis();
    do {
      if (features.getFeature(featureName) != null) {
        break;
      }
      Thread.sleep(500);
    } while (startup + 60_000 < System.currentTimeMillis());

    Feature feature;
    if ((feature = features.getFeature(featureName)) != null) {
      if (features.isInstalled(feature)) {
        fail("Feature " + featureName + " got installed before test was run. Please verify KAR packaging and related auto start options.");
      }

      features.installFeature(featureName, INSTALL_OPTIONS);
      assertFeatureInstalled(featureName);
    } else {
      fail("Feature " + featureName + " was not recognized by system");
    }
  }

  private void assertFeatureInstalled(String name) throws Exception {
    assertTrue("Feature " + name + " should be installed", features.isInstalled(features.getFeature(name)));
  }

}
