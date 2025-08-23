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
package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.feature.profile.ServerCoreEventHandler;
import eu.chargetime.ocpp.model.Request;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.ocpp.internal.OcppAttendant;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.connectorio.addons.binding.ocpp.internal.config.ServerConfig;
import org.connectorio.addons.binding.ocpp.internal.discovery.OcppChargerDiscoveryService;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.CompositeRequestListener;
import org.connectorio.addons.binding.ocpp.internal.server.OcppServer;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.AuthorizationIdTagAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.BootRegistrationAdapter;
import org.connectorio.addons.binding.ocpp.internal.server.adapter.RequestListenerAdapter;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

public class ServerBridgeHandler extends GenericBridgeHandlerBase<ServerConfig> implements OcppAttendant {

  private final CompositeRequestListener listener = new CompositeRequestListener();

  private NetworkAddressService networkAddressService;
  private OcppServer server;
  private ServerBridgeDispatcherAdapter bridgeHandler;

  public ServerBridgeHandler(Bridge bridge, NetworkAddressService networkAddressService) {
    super(bridge);
    this.networkAddressService = networkAddressService;
  }

  @Override
  public void initialize() {
    Optional<ServerConfig> thingConfig = getBridgeConfig();
    if (!thingConfig.isPresent()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "No configuration found");
      return;
    }

    ServerConfig config = thingConfig.get();
    String address = config.address;
    if (address == null || address.trim().isEmpty()) {
      address = networkAddressService.getPrimaryIpv4HostAddress();
    }
    if (config.port == 0) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Port setting missing");
      return;
    }

    Set<String> chargers = set(config.chargers);
    Set<String> tags = set(config.tags);

    BootRegistrationAdapter bootAdapter = new BootRegistrationAdapter(chargers);
    bridgeHandler = new ServerBridgeDispatcherAdapter(bootAdapter);
    Deque<ServerCoreEventHandler> eventHandlers = new ConcurrentLinkedDeque<>();
    // 2nd adapter
    eventHandlers.addFirst(new AuthorizationIdTagAdapter(tags));
    // 1st adapter
    eventHandlers.addFirst(bootAdapter);
    eventHandlers.add(bridgeHandler);
    eventHandlers.add(new RequestListenerAdapter(listener));

    server = new OcppServer(address, config.port, bootAdapter, eventHandlers);
    server.activate();
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void dispose() {
    if (server != null) {
      server.close();
    }
    if (bridgeHandler != null) {
      bridgeHandler = null;
    }
  }

  private Set<String> set(List<String> config) {
    return Optional.ofNullable(config)
      .map(values -> values.stream()
        .filter(text -> text != null && !text.trim().isEmpty())
        .collect(Collectors.toSet())
      )
      .filter(value -> !value.isEmpty())
      .orElse(Collections.emptySet());
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
    Object serial = childThing.getConfiguration().get(Thing.PROPERTY_SERIAL_NUMBER);
    if (serial instanceof String && bridgeHandler != null && childHandler instanceof ChargerThingHandler) {
      bridgeHandler.addHandler(new ChargerReference((String) serial), (ChargerThingHandler) childHandler);
    }
  }

  @Override
  public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
    Object serial = childThing.getConfiguration().get(Thing.PROPERTY_SERIAL_NUMBER);
    if (serial instanceof String && bridgeHandler != null) {
      bridgeHandler.removeHandler(new ChargerReference((String) serial));
    }
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(OcppChargerDiscoveryService.class);
  }

  @Override
  public <T extends Request> boolean addRequestListener(Class<T> type, OcppRequestListener<T> listener) {
    return this.listener.addRequestListener(type, listener);
  }

  @Override
  public <T extends Request> void removeRequestListener(OcppRequestListener<T> listener) {
    this.listener.removeRequestListener(listener);
  }
}
