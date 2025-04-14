/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.network.iface.linux.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;
import javax.measure.Quantity;
import org.connectorio.addons.network.iface.MetricCode;
import org.connectorio.addons.network.iface.NamedMetricCode;
import org.connectorio.addons.network.iface.NetworkInterfaceUID;
import org.connectorio.addons.network.iface.linux.internal.sysfs.MockSysfsReader;
import org.junit.jupiter.api.Test;

public class LinuxNetworkMetricsSupplierTest {

  @Test
  void testSupplier() {
    MockSysfsReader reader = new MockSysfsReader(Map.of(
      "/sys/class/net/eno0", List.of(),
      "/sys/class/net/eno0/statistics", List.of(),
      "/sys/class/net/eno0/statistics/tx_packets", List.of("5000"),
      "/sys/class/net/eno0/statistics/tx_bytes", List.of("1000")
    ));

    LinuxNetworkMetricsSupplier supplier = new LinuxNetworkMetricsSupplier(reader);
    supplier.fetch();
    Map<MetricCode, Quantity<?>> metrics = supplier.getMetrics(new NetworkInterfaceUID("eth", "eno0"));

    assertThat(metrics).isNotNull()
      .containsKey(new NamedMetricCode("tx_packets"))
      .containsKey(new NamedMetricCode("tx_bandwidth"))
      .containsKey(new NamedMetricCode("tx_packets_per_second"));
  }

}
