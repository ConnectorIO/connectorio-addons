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
package org.connectorio.addons.binding.fatek.config.channel.rollershutter;

import org.connectorio.addons.binding.fatek.config.channel.data.Data32ChannelConfig;
import org.simplify4u.jfatek.registers.RegName;

public class RollerShutter32ChannelConfig extends Data32ChannelConfig {

  public RegName upRegister;
  public int upIndex;
  public boolean upInvert;

  public RegName downRegister;
  public int downIndex;
  public boolean downInvert;

}
