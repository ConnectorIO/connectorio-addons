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
package org.connectorio.addons.binding.can.discovery;

import org.connectorio.addons.binding.can.CANInterface;

/**
 * Detection of available CAN interfaces is an abstract act which does not bring connectivity yet.
 *
 * This interface is intended to let implementers know that given CAN interface is available and can be utilized.
 */
public interface CANInterfaceDiscoveryDelegate {

  /**
   * Notify about availability of given CAN interface.
   *
   * @param iface CAN interface.
   */
  void interfaceAvailable(CANInterface iface);

}
