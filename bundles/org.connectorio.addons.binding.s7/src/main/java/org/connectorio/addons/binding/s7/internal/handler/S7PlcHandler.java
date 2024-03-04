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
package org.connectorio.addons.binding.s7.internal.handler;

import org.apache.plc4x.java.s7.readwrite.tag.S7Tag;
import org.connectorio.addons.binding.config.PollingConfiguration;
import org.connectorio.addons.binding.plc4x.config.CommonChannelConfiguration;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xThingHandler;
import org.connectorio.addons.binding.plc4x.source.BasicConverter;
import org.connectorio.addons.binding.plc4x.source.SourceFactory;
import org.connectorio.addons.binding.s7.S7BindingConstants;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;

public class S7PlcHandler extends PollingPlc4xThingHandler<S7Tag, S7NetworkBridgeHandler, PollingConfiguration> {

  public S7PlcHandler(Thing thing, SourceFactory sourceFactory) {
    super(thing, sourceFactory, new BasicConverter());
  }

  @Override
  public void initialize() {
    super.initialize();
  }

  @Override
  protected S7Tag createTag(Channel channel) {
    CommonChannelConfiguration configuration = channel.getConfiguration().as(CommonChannelConfiguration.class);
    return S7Tag.of(configuration.field);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return S7BindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
