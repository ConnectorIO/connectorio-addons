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
package org.connectorio.addons.binding.canopen.ta.tapi.dev;

import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TADigitalOutput;

public class DefaultObjectFactory implements ObjectFactory {

  public final TADevice device;

  public DefaultObjectFactory(TADevice device) {
    this.device = device;
  }

  @Override
  public TAAnalogOutput createAnalogOutput(int index, int unit, short value) {
    if (unit == -1) {
      return new TAAnalogOutput(device, false, index, 0, value);
    }
    return new TAAnalogOutput(device, index, unit, value);
  }

  @Override
  public TADigitalOutput createDigitalOutput(int index, int unit, boolean value) {
    if (unit == -1) {
      return new TADigitalOutput(device, false, index, unit, value);
    }
    return new TADigitalOutput(device, index, unit, value);
  }
}
