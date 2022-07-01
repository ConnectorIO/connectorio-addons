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
package org.connectorio.addons.binding.plc4x.canopen.internal.statistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.quantity.Quantities;

public class SysfsReader<Q extends Quantity<Q>> {

  private final Logger logger = LoggerFactory.getLogger(SysfsReader.class);
  private final String iface;
  private final String name;
  private final Unit<Q> unit;

  public SysfsReader(String iface, String name, Unit<Q> unit) {
    this.iface = iface;
    this.name = name;
    this.unit = unit;
  }

  public String getName() {
    return name;
  }

  protected Quantity<Q> readStatistic() {
    File statistic = new File("/sys/class/net/" + iface + "/statistics/" + name);

    if (statistic.isFile() && statistic.canRead()) {
      try {
        List<String> readings = Files.readAllLines(statistic.toPath());
        if (readings.size() == 1 && !readings.get(0).isEmpty()) {
          return Quantities.getQuantity(Long.parseLong(readings.get(0)), unit);
        } else {
          logger.debug("Can not map readings {} for statistic {}.", readings, statistic.getName());
        }
      } catch (IOException e) {
        logger.debug("Field to read can interface statistics.", e);
      }
    }
    return Quantities.getQuantity(0, unit);
  }

}
