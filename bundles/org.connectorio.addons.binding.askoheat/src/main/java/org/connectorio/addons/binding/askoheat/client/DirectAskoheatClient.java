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
package org.connectorio.addons.binding.askoheat.client;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.connectorio.addons.binding.askoheat.client.dto.CommandBlock;
import org.connectorio.addons.binding.askoheat.client.dto.ParameterBlock;
import org.connectorio.addons.binding.askoheat.client.dto.ValueBlock;
import org.connectorio.addons.binding.askoheat.client.dto.fullstatus.FullStatus;

/**
 * Implementation of Askoheat HTTP API through JAXRS client APIs
 */
public class DirectAskoheatClient implements AskoheatClient {

  private final Client client;
  private final WebTarget target;

  public DirectAskoheatClient(String uri) {
    this(ClientBuilder.newBuilder(), uri);
  }

  public DirectAskoheatClient(ClientBuilder clientBuilder, String uri) {
    this.client = clientBuilder.register(new JacksonJsonProvider())
      //.register(new LoggingFilter())
      .build();

    target = client.target(uri);
  }

  @Override
  public FullStatus getFullStatus() {
    return get("/STATUS.JSON", FullStatus.class);
  }

  @Override
  public ParameterBlock getParameterBlock() {
    return get("/GETPAR.JSON", ParameterBlock.class);
  }

  @Override
  public ValueBlock getValueBlock() {
    return get("/GETVAL.JSON", ValueBlock.class);
  }

  @Override
  public CommandBlock getCommandBlock() {
    return get("/GETCMD.JSON", CommandBlock.class);
  }

  @Override
  public CommandBlock sendCommandBlock(CommandBlock command) {
    Invocation invocation = target.path("/GETCMD.json").request(MediaType.APPLICATION_JSON_TYPE).buildPut(
      Entity.entity(command, MediaType.APPLICATION_JSON_TYPE)
    );
    return invocation.invoke(CommandBlock.class);
  }

  private <T> T get(String path, Class<T> responseType) {
    Invocation invocation = target.path(path).request(MediaType.APPLICATION_JSON_TYPE).buildGet();
    return invocation.invoke(responseType);
  }

}
