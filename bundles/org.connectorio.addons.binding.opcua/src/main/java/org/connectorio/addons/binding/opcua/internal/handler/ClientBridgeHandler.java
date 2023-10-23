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

package org.connectorio.addons.binding.opcua.internal.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.opcua.internal.config.ClientConfig;
import org.connectorio.addons.binding.opcua.internal.discovery.NodeDiscoveryService;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.SignedIdentityToken;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientBridgeHandler extends GenericBridgeHandlerBase<ClientConfig> {

  private final Logger logger = LoggerFactory.getLogger(ClientBridgeHandler.class);
  private CompletableFuture<OpcUaClient> clientConnection = new CompletableFuture<>();

  public ClientBridgeHandler(Bridge thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    getBridgeConfig().ifPresentOrElse(config -> connect(config).whenComplete((r, e) -> {
      if (e != null) {
        logger.error("Could not obtain connection to {}", config.host, e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        return;
      }
      clientConnection.complete(r);
      updateStatus(ThingStatus.ONLINE);
    }), () -> {
      updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing configuration");
    });
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Arrays.asList(
      NodeDiscoveryService.class
    );
  }

  private CompletableFuture<OpcUaClient> connect(ClientConfig config) {
    if (config.host == null || config.host.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing hostname");
      return CompletableFuture.completedFuture(null);
    }

    return DiscoveryClient.getEndpoints("opc.tcp://" + config.host + ":" + config.port)
      .thenApply(endpoints -> {
        for (EndpointDescription description : endpoints) {
          UserTokenPolicy[] tokens = description.getUserIdentityTokens();
          for (UserTokenPolicy tokenPolicy : tokens) {
            if (UserTokenType.Anonymous == tokenPolicy.getTokenType() || UserTokenType.UserName == tokenPolicy.getTokenType()) {
              return description;
            }
          }
        }
        throw new IllegalArgumentException();
      }).handle((endpointDescription, error) -> {
        new IdentityProvider() {
          @Override
          public SignedIdentityToken getIdentityToken(EndpointDescription endpointDescription, ByteString byteString) throws Exception {
            return null;
          }
        };

        OpcUaClientConfigBuilder clientConfig = OpcUaClientConfig.builder()
          .setApplicationName(LocalizedText.english("openHAB"))
          .setApplicationUri("urn:connectorio:binding:opcua:client")
          .setEndpoint(endpointDescription)
          .setRequestTimeout(UInteger.valueOf(config.requestTimeout == 0 ? 10_000 : config.requestTimeout))
          .setConnectTimeout(UInteger.valueOf(config.connectTimeout == 0 ? 10_000 : config.connectTimeout));
        if (config.username != null && !config.username.trim().isEmpty()) {
          clientConfig.setIdentityProvider(new UsernameProvider(config.username, config.password));
        }

        try {
          return OpcUaClient.create(clientConfig.build());
        } catch (UaException e) {
          throw new IllegalArgumentException("Could not create OPC UA client", e);
        }
      })
      .thenCompose(c -> {
        return c.connect().thenApply(OpcUaClient.class::cast);
      });

  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  public CompletionStage<OpcUaClient> getClient() {
    return clientConnection;
  }
}
