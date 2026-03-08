/*
 * Copyright (C) 2019-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.network.iface.linux.internal.sysfs;

import java.util.List;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.connectorio.addons.network.iface.linux.internal.StatisticCollector;
import org.connectorio.addons.network.iface.linux.internal.SysfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.quantity.Quantities;

public class SysfsStatisticCollector<Q extends Quantity<Q>> implements StatisticCollector<Q> {

  private final Logger logger = LoggerFactory.getLogger(SysfsStatisticCollector.class);
  private final SysfsReader reader;
  private final String statistic;
  private final Unit<Q> unit;

  public SysfsStatisticCollector(SysfsReader reader, String statistic, Unit<Q> unit) {
    this.reader = reader;
    this.statistic = statistic;
    this.unit = unit;
  }

  public Quantity<Q> getStatistic() {
    try {
      List<String> readings = reader.read(statistic);
      if (readings.size() == 1 && !readings.get(0).isEmpty()) {
        return Quantities.getQuantity(Long.parseLong(readings.get(0)), unit);
      } else {
        logger.debug("Can not map contents of file {} to plain statistic: {}.", statistic, readings);
      }
    } catch (Exception e) {
      logger.debug("Field to read network interface statistics through {}", reader, e);
    }

    return null;
  }

}
