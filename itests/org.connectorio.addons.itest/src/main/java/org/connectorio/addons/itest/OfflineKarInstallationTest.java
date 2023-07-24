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
package org.connectorio.addons.itest;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.connectorio.bddhab.rest.client.ApiException;
import org.connectorio.bddhab.rest.client.v31.AddonsApi;
import org.connectorio.bddhab.rest.client.v31.model.Addon;
import org.connectorio.testcontainers.openhab.std.UserAccountCustomization;
import org.connectorio.testcontainers.openhab.std.UserApiTokenCustomization;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.MountableFile;

import org.connectorio.bddhab.rest.client.ApiClient;
import org.connectorio.testcontainers.openhab.OpenHABContainer;

public abstract class OfflineKarInstallationTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Logger containerLogger = LoggerFactory.getLogger("");

  private final String assembly;
  private final String category;
  private final String addon;
  private final String feature;

  protected ApiClient client;

  protected String token;

  // fallback openhab version to latest 3.0.x release which is for now 3.0.4
  @Container
  protected OpenHABContainer<?> container = new OpenHABContainer<>("3.0.4")
    .withNetworkAliases("openhab")
    .withCustomization(new UserAccountCustomization("test", "test"))
    .withCustomization(new UserApiTokenCustomization())
    //.withDebug(5005)
    .withLogConsumer(new Slf4jLogConsumer(containerLogger));

  protected OfflineKarInstallationTest(String binding) {
    this(binding, "binding", binding);
  }

  protected OfflineKarInstallationTest(String assembly, String category, String addon) {
    this.client = new ApiClient();
    this.client.setRequestInterceptor(new Consumer<Builder>() {
      @Override
      public void accept(HttpRequest.Builder builder) {
        builder.header("X-OPENHAB-TOKEN", container.getCustomization(UserApiTokenCustomization.class).getApiToken());
      }
    });
    this.assembly = assembly;
    this.category = category;
    this.addon = addon;
    this.feature = category + "-" + addon;
  }

  @Before
  public void installAddonKar() throws Exception {
    Pattern pattern = Pattern.compile(".*Added feature repository 'mvn:org.connectorio.addons/.*" + assembly);

    CountDownLatch latch = new CountDownLatch(1);
    container.withLogConsumer(frame -> {
      Matcher matcher = pattern.matcher(frame.getUtf8String());
      if (matcher.find()) {
        latch.countDown();
      }
    });

    container.start(); // if not started yet?
    //logger.info("Debugger awaits connection at port {}", container.getMappedPort(5005));
    Integer port = container.getMappedPort(8080);
    this.client.updateBaseUri("http://localhost:" + port + "/rest/");

    File[] karFiles = new File("target/additional-resources")
      .listFiles(file -> file.getName().matches("org.connectorio.addons.kar." + assembly + "-.*\\.kar"));

    if (karFiles == null || karFiles.length != 1) {
      fail("Could not find KAR file for addon " + addon + ", matching files are " + (karFiles == null ? "none" : Arrays.toString(karFiles)));
      return;
    }

    logger.info("Found {} file for addon {}", karFiles[0].getAbsolutePath(), addon);
    container.deployKar(MountableFile.forHostPath(karFiles[0].getAbsolutePath()));

    boolean await = latch.await(3, TimeUnit.MINUTES);
    if (!await) {
      fail("Installation of KAR for " + addon + " did not complete within expected time frame");
    }
  }

  @Test
  public void testOfflineFeatureInstallation() throws Exception {
    AddonsApi addonsApi = new AddonsApi(client);
    // OH 3.0 has malformed API descriptor without schema for Addon types
    addonsApi.installAddonById(feature);

    Set<Addon> availableAddons = new LinkedHashSet<>();
    Awaitility.await("addon " + addon + " is installed").pollDelay(Duration.ofSeconds(5)).atMost(Duration.ofMinutes(2))
      .ignoreException(ApiException.class).until(() -> {
        boolean newAddonsFound = availableAddons.addAll(addonsApi.getAddons(null));

        Optional<Addon> available = availableAddons.stream()
          .filter(info -> info.getId().contains(addon) && info.getInstalled())
          .findFirst();

        return available.map(addonObj -> true)
          .orElseGet(() -> {
            if (newAddonsFound) {
              logger.info("Addon {} is not installed yet, currently available: {}", addon, availableAddons);
            }
            return false;
          });
      });
  }

}
