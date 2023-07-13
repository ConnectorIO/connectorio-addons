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
package org.connectorio.addons.binding.canopen.internal.handler;

import java.util.Optional;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.config.CoPdoConfig;
import org.connectorio.addons.binding.canopen.internal.provider.CoPdoChannelTypeProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base handler type responsible for Process Data Object handling (PDO). PDO's are one way message exchanges which happen
 * in asynchronous matter over can bus. They have no overhead, however they must be mapped by sender and receiver.
 *
 * @param <C> Configuration type.
 */
public abstract class AbstractPdoHandler<C extends CoPdoConfig> extends BaseThingHandler {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final Class<C> configType;
  protected C config;
  protected PdoTemplate template;

  public AbstractPdoHandler(Thing thing, Class<C> configType) {
    super(thing);
    this.configType = configType;
  }

  @Override
  public void initialize() {
    CoNodeBridgeHandler handler = Optional.ofNullable(getBridge()).map(Bridge::getHandler)
      .filter(CoNodeBridgeHandler.class::isInstance)
      .map(CoNodeBridgeHandler.class::cast)
      .orElse(null);

    if (handler == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
      return;
    }

    this.config = getConfigAs(configType);
    this.template = new PdoTemplate();
    for (Channel channel : getThing().getChannels()) {
      CANOpenDataType type = CoPdoChannelTypeProvider.typeFromChannel(channel.getChannelTypeUID());
      if (type != null) {
        template.add(type, channel);
      } else {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Channel " + channel.getUID() + " does not map to any CANopen data type");
        return;
      }
    }

    handler.getNode().whenComplete((result, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, error.getMessage());
        return;
      }

      doInitialize(result);
    });
  }

  protected abstract void doInitialize(CoNode result);

}
