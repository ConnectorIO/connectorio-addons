/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.transformation.inverse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The implementation of {@link TransformationService} which replaces.
 *
 * <p>
 * <b>Note:</b> the given Regular Expression must contain exactly one group!
 *
 * @author Thomas.Eichstaedt-Engelen
 */
@Component(immediate = true, property = {"smarthome.transform=INVERSE"})
public class InverseTransformationService implements TransformationService {

  private static final Pattern SUBSTR_PATTERN = Pattern.compile("([^:]+):([^:]+)");
  private final Logger logger = LoggerFactory.getLogger(InverseTransformationService.class);

  @Override
  public String transform(String inverseStr, String source) throws TransformationException {
    if (inverseStr == null || source == null) {
      throw new TransformationException("the given parameters 'inverseStr' and 'source' must not be null");
    }

    logger.debug("about to transform '{}' by the function '{}'", source, inverseStr);
    String result = source;

    Matcher substMatcher = SUBSTR_PATTERN.matcher(inverseStr);
    if (substMatcher.matches()) {
      logger.debug("Using substitution form of regex transformation");
      String first = substMatcher.group(1);
      String second = substMatcher.group(2);

      result = result.replace(first, second);
    }

    return result;
  }

}
