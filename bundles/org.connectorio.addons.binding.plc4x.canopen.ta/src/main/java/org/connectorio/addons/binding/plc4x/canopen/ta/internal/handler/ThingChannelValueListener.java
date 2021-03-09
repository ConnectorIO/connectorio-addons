/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import java.util.Optional;
import java.util.function.Function;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.OldAnalogChannelConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAValue;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ThingChannelValueListener implements ValueListener {

  private final Logger logger = LoggerFactory.getLogger(ThingChannelValueListener.class);
  private final ThingHandlerCallback callback;
  private final Thing thing;
  private final Function<TAValue, State> converter;

  ThingChannelValueListener(ThingHandlerCallback callback, Thing thing, Function<TAValue, State> converter) {
    this.callback = callback;
    this.thing = thing;
    this.converter = converter;
  }

  @Override
  public void analog(int index, ReadBuffer buffer) throws ParseException {
    short val = buffer.readShort(16);
    Channel channel = thing.getChannel("analog#" + index);
    AnalogUnit unit = Optional.ofNullable(channel.getConfiguration())
      .map(cfg -> cfg.as(OldAnalogChannelConfig.class))
      .map(cfg -> cfg.unit)
      .orElse(AnalogUnit.DIMENSIONLESS);

    TAValue value = new TAValue(unit.getIndex(), val);

    logger.info("Analog channel {} (index {}) value {}", channel.getUID(), index, value);

    Optional.ofNullable(callback).ifPresent(callback -> callback.stateUpdated(channel.getUID(), converter.apply(value)));
  }

  @Override
  public void digital(int index, boolean value) {
    Channel channel = thing.getChannel("digital#" + index);

    if (channel != null) {
      logger.info("Digital channel {} (index {}) value {}", channel.getUID(), index, value);
      Optional.ofNullable(callback)
        .ifPresent(callback -> callback.stateUpdated(channel.getUID(), value ? OpenClosedType.OPEN : OpenClosedType.CLOSED));
    } else {
      logger.trace("Unknown digital channel digital#{}", index);
    }
  }
}
