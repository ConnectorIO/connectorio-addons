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
package org.connectorio.addons.ui.iconify.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.ui.icon.AbstractResourceIconProvider;
import org.openhab.core.ui.icon.IconProvider;
import org.openhab.core.ui.icon.IconSet;
import org.openhab.core.ui.icon.IconSet.Format;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iconify icon provider is based on Iconify icon sets.
 *
 * Provider itself is rather plain as it does rely on assets which are generated during build time.
 */
@Component(immediate = true, service = IconProvider.class)
public class IconifyIconProvider extends AbstractResourceIconProvider {

  public static final String ICONS_LOCATION = "/icons";
  public static final String DEFAULT_ICONSET_ID = "classic";

  private final Logger logger = LoggerFactory.getLogger(IconifyIconProvider.class);

  private final Set<IconSet> iconSets = new HashSet<>();
  private final Map<String, Map<String, URL>> icons = new HashMap<>();

  @Activate
  public IconifyIconProvider(@Reference TranslationProvider i18nProvider, BundleContext context) {
    super(i18nProvider);

    // register classic iconset to feed iconics also as default icons!
    Map<String, URL> classicOverrides = new HashMap<>();
    iconSets.add(new IconSet(DEFAULT_ICONSET_ID, "Classic overrides", "", Collections.singleton(Format.SVG)));
    icons.put(DEFAULT_ICONSET_ID, classicOverrides);

    try {
      Enumeration<String> resources = context.getBundle().getEntryPaths(ICONS_LOCATION);
      while (resources.hasMoreElements()) {
        String id = resources.nextElement();
        id = id.substring(ICONS_LOCATION.length(), id.length() - 1);
        IconSet iconSet  = new IconSet(id,
          "Iconify " + id, "Iconify icon set",
          Collections.singleton(Format.SVG)
        );
        iconSets.add(iconSet);
        logger.info("Detected iconify iconset {}", id);

        Map<String, URL> assets = new HashMap<>();
        String iconSetPath = ICONS_LOCATION + "/" + id;
        Enumeration<String> iconFiles = context.getBundle().getEntryPaths(iconSetPath);
        while (iconFiles.hasMoreElements()) {
          String icon = iconFiles.nextElement();
          URL resource = context.getBundle().getResource(icon);

          icon = icon.substring(iconSetPath.length());
          logger.debug("Found icon {}", icon);
          assets.put(icon.toLowerCase(), resource);
          classicOverrides.put(id + "-" + icon.toLowerCase(), resource);
        }
        icons.put(id, assets);
      }
    } catch (Exception e) {
      logger.warn("Could not load Monos icons", e);
    }
  }

  @Override
  protected Integer getPriority() {
    return 10;
  }

  @Override
  protected InputStream getResource(String iconSetId, String resourceName) {
    try {
      // swap underscore with dash as dash has special meaing for openhab
      if (resourceName.contains("_")) {
        resourceName = resourceName.replaceAll("_", "-");
      }
      return icons.get(iconSetId).get(resourceName).openStream();
    } catch (IOException e) {
      logger.warn("Failed to retrieve icon {} from iconify icon set {}", resourceName, iconSetId, e);
      return null;
    }
  }

  @Override
  protected boolean hasResource(String iconSetId, String resourceName) {
    return icons.containsKey(iconSetId) && (hasIconResource(iconSetId, resourceName) || hasIconResource(iconSetId, resourceName.replaceAll("_", "-")));
  }

  @Override
  public Set<IconSet> getIconSets(Locale locale) {
    return Collections.unmodifiableSet(iconSets);
  }

  private boolean hasIconResource(String iconSetId, String resourceName) {
    return icons.get(iconSetId).containsKey(resourceName);
  }

}
