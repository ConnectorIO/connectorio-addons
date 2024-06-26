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
package org.connectorio.addons.binding.fatek.config.channel;

import org.connectorio.addons.binding.config.PollingConfiguration;
import org.simplify4u.jfatek.registers.RegName;

public abstract class BaseChannelConfig extends PollingConfiguration implements RegisterConfig {

  public int index;

  public RegName register;

  public BaseChannelConfig() {}
  public BaseChannelConfig(RegName register, int index) {
    this.register = register;
    this.index = index;
  }

  @Override
  public RegName getRegister() {
    return register;
  }

  @Override
  public int getIndex() {
    return index;
  }

}
