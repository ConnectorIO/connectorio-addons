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
package org.connectorio.addons.binding.amsads.internal.handler;

import static org.connectorio.addons.binding.amsads.AmsAdsBindingConstants.THING_TYPE_AMS;
import static org.connectorio.addons.binding.amsads.AmsAdsBindingConstants.THING_TYPE_NETWORK;
import static org.connectorio.addons.binding.amsads.AmsAdsBindingConstants.THING_TYPE_SERIAL;

import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsDiscoveryDriver;
import org.connectorio.addons.binding.amsads.internal.handler.channel.ChannelHandlerFactory;
import org.connectorio.addons.binding.amsads.internal.handler.channel.DefaultChannelHandlerFactory;
import org.connectorio.addons.binding.amsads.internal.symbol.DefaultSymbolReaderFactory;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReaderFactory;
import org.connectorio.addons.binding.plc4x.Plc4xHandlerFactory;
import org.connectorio.addons.binding.plc4x.source.SourceFactory;
import org.connectorio.plc4x.extras.osgi.PlcDriverManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link AmsAdsHandlerFactory} is responsible for creating handlers fod ads enabled elements.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.amsads", service = ThingHandlerFactory.class)
public class AmsAdsHandlerFactory extends Plc4xHandlerFactory {

  private final PlcDriverManager driverManager;
  private final SourceFactory sourceFactory;

  private final SymbolReaderFactory symbolReaderFactory = new DefaultSymbolReaderFactory();

  private final ChannelHandlerFactory channelHandlerFactory = new DefaultChannelHandlerFactory();

  private AmsAdsDiscoveryDriver discoveryDriver;

  @Activate
  public AmsAdsHandlerFactory(@Reference PlcDriverManager driverManager, @Reference(target = "(plc4x=true)") SourceFactory sourceFactory) {
    super(THING_TYPE_AMS, THING_TYPE_NETWORK, THING_TYPE_SERIAL);
    this.driverManager = driverManager;
    this.sourceFactory = sourceFactory;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_AMS.equals(thingTypeUID)) {
      return new AmsBridgeHandler((Bridge) thing, discoveryDriver);
    } else if (THING_TYPE_NETWORK.equals(thingTypeUID)) {
      return new AmsAdsNetworkBridgeHandler(thing, symbolReaderFactory, channelHandlerFactory, driverManager, sourceFactory, discoveryDriver);
    } else if (THING_TYPE_SERIAL.equals(thingTypeUID)) {
      return new AmsAdsSerialBridgeHandler(thing, symbolReaderFactory, channelHandlerFactory,
          driverManager, sourceFactory);
    }

    return null;
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  void setDiscoveryDriver(AmsAdsDiscoveryDriver discoveryDriver) {
    this.discoveryDriver = discoveryDriver;
  }

  void unsetDiscoveryDriver(AmsAdsDiscoveryDriver discoveryDriver) {
    this.discoveryDriver = null;
  }
}
