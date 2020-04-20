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
package org.connectorio.telemetry.api;

/**
 * Telemetry service supports two operations - sending an telemetry and obtaining telemetry identifier.
 *
 * Because telemetry in environments such as openHAB is run on third party infrastructure it can be easily spoofed and
 * spammed with scripted devices. In order to avoid that Telemetry reporting requires device token which can be obtained
 * via pairing servlet.
 */
public interface TelemetryService {

  TelemetryClientId generateId();

  void upload(TelemetryClientId id, MeteredValue<?> measure);

  class TelemetryClientId {
    String id;
  }

  class MeteredValue<T extends Measured> {

  }

  interface Measured {

  }

}
