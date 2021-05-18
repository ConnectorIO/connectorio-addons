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
import java.util.ArrayList;
import java.util.List;
import org.connectorio.addons.binding.can.statistic.CANStatisticCollector;
import org.openhab.core.library.unit.Units;

/**
 * Creation of socketcan specific statistic collectors.
 */
public abstract class SocketCANStatisticCollectors {

  public static List<CANStatisticCollector> create(String iface) {
    List<CANStatisticCollector> collectors = new ArrayList<>();
    File statistics = new File("/sys/class/net/" + iface + "/statistics");

    if (statistics.isDirectory() && statistics.canRead()) {
      collectors.add(new SysfsStatisticCollector<>(iface, "rx_bytes", Units.BYTE));
      collectors.add(new SysfsStatisticCollector<>(iface, "tx_bytes", Units.BYTE));
      collectors.add(new SysfsStatisticCollector<>(iface, "rx_packets", Units.ONE));
      collectors.add(new SysfsStatisticCollector<>(iface, "tx_packets", Units.ONE));

      collectors.add(new BandwidthStatisticCollector(System::currentTimeMillis, iface, "rx_bytes"));
      collectors.add(new BandwidthStatisticCollector(System::currentTimeMillis, iface, "tx_bytes"));
      collectors.add(new RatioStatisticCollector(System::currentTimeMillis, iface, "rx_packets"));
      collectors.add(new RatioStatisticCollector(System::currentTimeMillis, iface, "tx_packets"));
    }
    return collectors;
  }

}
