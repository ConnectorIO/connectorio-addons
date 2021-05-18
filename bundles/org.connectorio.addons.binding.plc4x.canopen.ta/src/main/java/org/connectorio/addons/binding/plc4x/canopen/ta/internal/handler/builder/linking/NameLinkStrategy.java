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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.builder.linking;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanInputObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanInputObject.InputConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanOutputObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NameLinkStrategy implements InputObjectLinkStrategy {

  private final Logger logger = LoggerFactory.getLogger(NameLinkStrategy.class);

  @Override
  public CompletableFuture<ObjectKey> get(TACanOutputObject<?> object, List<TACanInputOutputObject<?>> objects) {
    // just a placeholder to chain all futures together
    CompletableFuture<ObjectKey> input = CompletableFuture.completedFuture(new ObjectKey(TACanOutputObject.class, -1));
    if (objects.isEmpty()) {
      logger.debug("Resigning from matching outputs and inputs. No inputs to match.");
      return input;
    }

    logger.debug("Attempting to match CAN Output #{} with {} inputs {}.", object.getIndex(), objects.size(), objects);

    for (TACanInputOutputObject<?> reverse : objects) {
      if (object == reverse) {
        continue;
      }

      if (object instanceof TAAnalogOutput && reverse instanceof TAAnalogInput) {
        ObjectKey objectKey = compute(object, (TACanInputObject<?>) reverse);
        if (objectKey != null) {
          return CompletableFuture.completedFuture(objectKey);
        }
      }

      if (object instanceof TADigitalOutput && reverse instanceof TADigitalInput) {
        ObjectKey objectKey = compute(object, (TACanInputObject<?>) reverse);
        if (objectKey != null) {
          return CompletableFuture.completedFuture(objectKey);
        }
      }
    }

    return input;
  }

  private ObjectKey compute(TACanOutputObject<?> object, TACanInputObject<?> reverse) {
    String outputName = object.getName().getNow("");
    String inputName = reverse.getName().getNow("");

    boolean matched = outputName.equals(inputName);
    logger.trace("CAN Output #{} and CAN Input #{} match status: {}. Names: '{}', '{}'", object.getIndex(), reverse.getIndex(), matched, outputName, inputName);

    if (matched) {
      int reverseUnit = reverse.getConfiguredUnit();
      if (object.getUnit() != reverseUnit && reverseUnit != InputConfig.UNIT_AUTOMATIC) {
        logger.warn("Detected failure scenario input and output units do not match. Output unit {}, input unit {}", object.getUnit(),
          reverseUnit);
        return null;
      }

      if (reverseUnit == InputConfig.UNIT_AUTOMATIC) {
        logger.info("Overriding unit of {} with unit of {}", reverse, object);
        // automatic unit handling - we take it from matching output
        reverse.setUnit(object.getUnit());
      }
      return new ObjectKey(reverse.getClass(), reverse.getIndex());
    }

    logger.trace("CAN Output #{} does not match CAN Input #{} name {} {}", object.getIndex(), reverse.getIndex(), outputName, inputName);
    return null;
  }
}
