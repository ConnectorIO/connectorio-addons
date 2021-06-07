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

import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import org.connectorio.addons.binding.relayweblog.client.dto.Login;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterInfo;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterReading;
import org.connectorio.addons.binding.relayweblog.client.dto.Session;

/**
 * High level definition of weblog REST API.
 */
public interface WeblogClient {

  void login(String passwordHash, SigningContext signingContext);

  List<MeterInfo> getMeters();

  List<MeterReading> getReadings(String id);
}
