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
package org.connectorio.addons.binding.fatek.internal.channel.converter;

import org.connectorio.addons.binding.fatek.config.channel.binary.DiscreteChannelConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValueDis;

public class ContactConverter implements Converter {

  private final DiscreteChannelConfig config;

  public ContactConverter(DiscreteChannelConfig config) {
    this.config = config;
  }

  @Override
  public RegValue toValue(Command command) {
    return null;
  }

  @Override
  public State toState(RegValue value) {
    boolean status = config.invert ? !value.boolValue() : value.boolValue();
    return status ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
  }
}
