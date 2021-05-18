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
package org.connectorio.addons.binding.plc4x.canopen.internal.provider;

import java.util.Arrays;
import java.util.List;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.openhab.core.thing.ThingTypeUID;

public class ThingTypeDef {

  private final ThingTypeUID type;
  private final String label;
  private final boolean receivePdo;
  private final List<CANOpenService> services;

  public ThingTypeDef(ThingTypeUID type, String label, boolean receivePdo, CANOpenService ... services) {
    this.type = type;
    this.label = label;
    this.receivePdo = receivePdo;
    this.services = Arrays.asList(services);
  }

  public ThingTypeUID getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  public boolean isReceivePdo() {
    return receivePdo;
  }

  public List<CANOpenService> getServices() {
    return services;
  }

}
