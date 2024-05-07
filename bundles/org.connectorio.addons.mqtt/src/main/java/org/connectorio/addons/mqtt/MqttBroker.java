/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.mqtt;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizatorPolicy;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openhab.binding.mqtt.MqttBindingConstants;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ThingProvider.class)
public class MqttBroker implements ThingProvider {

  private final List<ProviderChangeListener<Thing>> listeners = new CopyOnWriteArrayList<>();

  private final Logger logger = LoggerFactory.getLogger(MqttBroker.class);
  private Server server;
  private Thing brokerThing;

  @Activate
  public MqttBroker() {
    server = new Server();
  }

  @Activate
  public void start() throws IOException {
    IAuthenticator authenticator = new IAuthenticator() {
      @Override
      public boolean checkValid(String clientId, String username, byte[] password) {
        logger.debug("Attempt to authenticate client {} with username {}", clientId, username);
        return username != null && username.equals(clientId) && Arrays.equals(username.getBytes(), password);
      }
    };
    IAuthorizatorPolicy authorizer = null;
    server.startServer(new MemoryConfig(new Properties()), Collections.emptyList(), null, authenticator, authorizer);

    Map<String, Object> cfg = new HashMap<>();
    cfg.put("name", "system");
    cfg.put("host", "127.0.0.1");
    cfg.put("port", 1883);
    cfg.put("secure", false);
    cfg.put("clientID", "system");
    cfg.put("username", "system");
    cfg.put("password", "system");
    brokerThing = BridgeBuilder.create(MqttBindingConstants.BRIDGE_TYPE_BROKER, "system")
      .withLabel("System broker")
      .withConfiguration(new Configuration(cfg))
      .build();
    listeners.forEach(listener -> listener.added(this, brokerThing));
  }

  @Deactivate
  public void stop() {
    listeners.forEach(listener -> listener.removed(this, brokerThing));
    brokerThing = null;

    if (server != null) {
      server.stopServer();
    }

  }

  @Override
  public Collection<Thing> getAll() {
    return brokerThing != null ? Collections.singleton(brokerThing) : Collections.emptyList();
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<Thing> listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<Thing> listener) {
    this.listeners.remove(listener);
  }

}
