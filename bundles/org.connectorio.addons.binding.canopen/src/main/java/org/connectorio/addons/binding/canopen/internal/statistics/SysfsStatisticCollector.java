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
package org.connectorio.addons.binding.canopen.internal.statistics;

import javax.measure.Quantity;
import javax.measure.Unit;
import org.connectorio.addons.binding.can.statistic.CANStatisticCollector;

/**
 * Statistic collector which reads linux /sys/class/net filesystem in pursue of CAN interface statistics.
 *
 * Below logic is very simple and can be in fact adopted to any network interface.
 */
class SysfsStatisticCollector<Q extends Quantity<Q>> extends SysfsReader<Q> implements CANStatisticCollector<Q> {

  public SysfsStatisticCollector(String iface, String name, Unit<Q> unit) {
    super(iface, name, unit);
  }

  @Override
  public Quantity<Q> getStatistic() {
    return readStatistic();
  }

}
