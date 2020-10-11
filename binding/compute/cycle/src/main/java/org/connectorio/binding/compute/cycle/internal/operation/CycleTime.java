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
package org.connectorio.binding.compute.cycle.internal.operation;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.connectorio.binding.compute.cycle.internal.CycleOperation;
import org.connectorio.binding.compute.cycle.internal.config.CycleCounterConfig;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.quantity.time.TimeQuantities;

public class CycleTime implements CycleOperation {

  private final Logger logger = LoggerFactory.getLogger(CycleTime.class);

  private final Supplier<Long> clock;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;
  private final CycleCounterConfig config;
  private final AtomicLong start = new AtomicLong();

  public CycleTime(Supplier<Long> clock, ThingHandlerCallback callback, ChannelUID channelUID, CycleCounterConfig config) {
    this.clock = clock;
    this.callback = callback;
    this.channelUID = channelUID;
    this.config = config;
  }

  @Override
  public void open() {
    start.set(clock.get());
  }

  @Override
  public void close() {
    Long stop = clock.get();
    long cycleTime = stop - start.get();
    logger.trace("Calculated cycle time {} ({} - {})", cycleTime, stop, start.get());
    callback.stateUpdated(channelUID, QuantityType.valueOf(cycleTime, TimeQuantities.MILLISECOND));
  }

  @Override
  public ChannelUID getChannelId() {
    return channelUID;
  }

}
