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
package org.connectorio.telemetry.model.v1;

import java.util.Collections;
import java.util.Map;
import org.connectorio.telemetry.model.Telemetry;

public class TelemetryV1 implements Telemetry {

  private final long timestamp;
  private String classification;
  private Map<? extends String, ?> attributes;

  public TelemetryV1(String classification, Map<? extends String, ?> attributes) {
    this(System.currentTimeMillis(), classification, attributes);
  }

  public TelemetryV1(long timestamp, String classification, Map<? extends String, ?> attributes) {
    this.timestamp = timestamp;
    this.classification = classification;
    this.attributes = attributes;
  }

  @Override
  public final int getVersion() {
    return 1;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String getClassification() {
    return classification;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }
}
