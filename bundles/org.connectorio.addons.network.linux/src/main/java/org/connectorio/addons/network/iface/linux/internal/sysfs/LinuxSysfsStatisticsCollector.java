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

import static java.util.Map.entry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.connectorio.addons.network.iface.linux.internal.StatisticProcessor;
import org.connectorio.addons.network.iface.linux.internal.SysfsReader;
import org.connectorio.addons.network.iface.linux.internal.StatisticsCollector;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common linux network interface statistic collector - rely on sysfs entries.

 * Additionally, on top of collected data, this collector will create a derivatives: rx/tx bandwidth
 * as well as rx/tx packets per second.
 */
public class LinuxSysfsStatisticsCollector implements StatisticsCollector {

  private final static Map<String, Unit<?>> MAPPING = Map.ofEntries(
    entry("rx_bytes", Units.BYTE),
    entry("tx_bytes", Units.BYTE),
    // dimensionless
    entry("collisions", Units.ONE),
    entry("multicast", Units.ONE),
    entry("rx_compressed", Units.ONE),
    entry("rx_crc_errors", Units.ONE),
    entry("rx_dropped", Units.ONE),
    entry("rx_errors", Units.ONE),
    entry("rx_fifo_errors", Units.ONE),
    entry("rx_frame_errors", Units.ONE),
    entry("rx_length_errors", Units.ONE),
    entry("rx_missed_errors", Units.ONE),
    entry("rx_nohandler", Units.ONE),
    entry("rx_over_errors", Units.ONE),
    entry("rx_packets", Units.ONE),
    entry("tx_aborted_errors", Units.ONE),
    entry("tx_carrier_errors", Units.ONE),
    entry("tx_compressed", Units.ONE),
    entry("tx_dropped", Units.ONE),
    entry("tx_errors", Units.ONE),
    entry("tx_fifo_errors", Units.ONE),
    entry("tx_heartbeat_errors", Units.ONE),
    entry("tx_packets", Units.ONE),
    entry("tx_window_errors", Units.ONE)
  );

  private final static Map<String, StatisticProcessor> PROCESSORS = Map.ofEntries(
    entry("rx_bandwidth", new BandwidthStatisticProcessor("rx_bytes")),
    entry("tx_bandwidth", new BandwidthStatisticProcessor("tx_bytes")),
    entry("rx_packets_per_second", new RatioStatisticProcessor("rx_packets")),
    entry("tx_packets_per_second", new RatioStatisticProcessor("tx_packets"))
  );

  private final Logger logger = LoggerFactory.getLogger(LinuxSysfsStatisticsCollector.class);

  private final Supplier<Long> clock;
  private final SysfsReader reader;

  public LinuxSysfsStatisticsCollector(SysfsReader reader) {
    this(System::currentTimeMillis, reader);
  }

  public LinuxSysfsStatisticsCollector(Supplier<Long> clock, SysfsReader reader) {
    this.clock = clock;
    this.reader = reader;
  }

  public Map<String, Quantity<?>> collect(String iface) {
    SysfsReader statisticReader = reader.narrow(iface, "statistics");

    Map<String, Quantity<?>> collected = new HashMap<>();
    Set<String> stats = statisticReader.list();

    logger.debug("Retrieving statistics {} of network interface {} through sysfs", stats, iface);
    for (String stat : stats) {
      Unit<?> unit = MAPPING.getOrDefault(stat, Units.ONE);
      SysfsStatisticCollector<?> collector = new SysfsStatisticCollector<>(statisticReader, stat, unit);
      Quantity<?> statistic = collector.getStatistic();
      if (statistic != null) {
        collected.put(stat, statistic);
      }
    }

    for (Entry<String, StatisticProcessor> entry : PROCESSORS.entrySet()) {
      Quantity<?> processed = entry.getValue().process(clock, collected);
      if (processed != null) {
        collected.put(entry.getKey(), processed);
      }
    }

    return collected;
  }

}
