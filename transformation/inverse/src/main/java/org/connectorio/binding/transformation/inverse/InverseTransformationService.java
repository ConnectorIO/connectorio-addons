/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional information.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.connectorio.binding.transformation.inverse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
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
