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
package org.connectorio.addons.binding.relayweblog.client;

import static org.connectorio.addons.binding.relayweblog.client.Hash.sha512;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.util.List;
import java.util.function.Supplier;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.connectorio.addons.binding.relayweblog.client.dto.Login;
import org.connectorio.addons.binding.relayweblog.client.dto.Meter;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterInfo;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterList;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterReading;
import org.connectorio.addons.binding.relayweblog.client.dto.Session;
import org.connectorio.addons.binding.relayweblog.client.filter.LoggingFilter;
import org.connectorio.addons.binding.relayweblog.client.filter.SigningFilter;

public class DirectWeblogClient implements WeblogClient {

  private final Client client;
  private final WebTarget target;
  private final String passwordHash;
  private final SigningContext signingContext;

  public DirectWeblogClient(String uri, String password) {
    this(ClientBuilder.newBuilder(), uri, password);
  }

  public DirectWeblogClient(ClientBuilder clientBuilder, String uri, String password) {
    this.passwordHash = sha512(password);
    this.signingContext = new SigningContext(passwordHash);

    this.client = clientBuilder.register(new JacksonJsonProvider())
      .register(new SigningFilter(uri, signingContext))
      .register(new LoggingFilter())
      .build();

    target = client.target(uri);
    login(passwordHash, signingContext);
  }

  @Override
  public void login(String passwordHash, SigningContext signingContext) {
    Invocation invocation = target.path("/api/v1/auth/ops/login").request()
      .header("Accept", MediaType.APPLICATION_JSON)
      .buildPost(Entity.json(new Login(passwordHash, "oQQlYwJ4rRfs6P6Z")));
    Session session = invocation.invoke(Session.class);

    if (signingContext.getSession() == null) {
      signingContext.setSession(session.getToken());
    }
  }

  @Override
  public List<MeterInfo> getMeters() {
    return call(() -> {
      Invocation invocation = target.path("/api/v1/domain/meterlist").request()
        .header("Accept", MediaType.APPLICATION_JSON)
        .buildGet();

      MeterList meterList = invocation.invoke(MeterList.class);
      return meterList.getMeters();
    });
  }

  @Override
  public List<MeterReading> getReadings(final String id) {
    return call(() -> {
      Invocation invocation = target.queryParam("meter_uid", id)
        .path("/api/v1/domain/meterlist/read_values").request()
        .header("Accept", MediaType.APPLICATION_JSON)
        .buildGet();

      Meter meter = invocation.invoke(Meter.class);
      return meter.getReadings();
    });
  }

  private <T> T call(Supplier<T> logic) {
    try {
      return logic.get();
    } catch (NotAuthorizedException e) {
      signingContext.resetSession();
      login(passwordHash, signingContext);
      return logic.get();
    }
  }

  public static void main(String[] args) {
    String password = "00001767";

    WeblogClient client = new DirectWeblogClient("http://localhost:18080", password);
    List<MeterInfo> meters = client.getMeters();

    for (MeterInfo meter : meters) {
      System.out.println(meter.getId() + ": " + meter.getIdentifier() + " " + meter.getManufacturer() + " " + meter.getMedium());
      List<MeterReading> readings = client.getReadings(meter.getId());
      for (MeterReading reading : readings) {
        System.out.println("\t" + reading.getName() + " " + reading.getValue() + " " + reading.getUnit());
        break;
      }
      System.out.println("===");
    }
  }

}
