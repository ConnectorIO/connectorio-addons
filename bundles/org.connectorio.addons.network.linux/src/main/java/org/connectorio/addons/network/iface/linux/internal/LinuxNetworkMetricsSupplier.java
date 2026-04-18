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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.measure.Quantity;
import org.connectorio.addons.network.iface.NamedMetricCode;
import org.connectorio.addons.network.iface.MetricsSupplier;
import org.connectorio.addons.network.iface.MetricCode;
import org.connectorio.addons.network.iface.NetworkInterfaceUID;
import org.connectorio.addons.network.iface.linux.internal.sysfs.LinuxSysfsStatisticsCollector;
import org.connectorio.addons.network.iface.linux.internal.sysfs.PlainSysfsReader;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LinuxNetworkMetricsSupplier implements MetricsSupplier {

  private final Logger logger = LoggerFactory.getLogger(LinuxNetworkMetricsSupplier.class);
  private final Map<String, Map<MetricCode, Quantity<?>>> statistics = new ConcurrentHashMap<>();
  private final SysfsReader reader;
  private final ScheduledExecutorService executor;
  private final long sampleTimeMs;

  @Activate
  public LinuxNetworkMetricsSupplier(@Reference SysfsReader reader) {
    this(reader.narrow("class", "net"), Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "linux-nic-stat-collector");
      thread.setDaemon(true);
      return thread;
    }), 60_000);
  }

  public LinuxNetworkMetricsSupplier(SysfsReader reader, ScheduledExecutorService executor, long sampleTimeMs) {
    this.reader = reader;
    this.executor = executor;
    this.sampleTimeMs = sampleTimeMs;

    executor.scheduleAtFixedRate(this::fetch, 0, sampleTimeMs, TimeUnit.MILLISECONDS);
  }

  void fetch() {
    Set<String> registeredInterfaces = new HashSet<>(statistics.keySet());
    for (String nif : reader.list()) {
      LinuxSysfsStatisticsCollector collector = new LinuxSysfsStatisticsCollector(reader);
      Map<String, Quantity<?>> stats = collector.collect(nif);
      Map<MetricCode, Quantity<?>> interfaceMetrics = new LinkedHashMap<>();
      for (Map.Entry<String, Quantity<?>> entry : stats.entrySet()) {
        interfaceMetrics.put(new NamedMetricCode(entry.getKey()), entry.getValue());
      }
      // flush stats into statistics map
      statistics.put(nif, interfaceMetrics);
      registeredInterfaces.remove(nif);
    }

    if (!registeredInterfaces.isEmpty()) {
      logger.info("Detected removal of network interfaces: {}. Cleaning up resources.", registeredInterfaces);
      for (String nic : registeredInterfaces) {
        statistics.remove(nic);
      }
    }
  }

  @Deactivate
  public void shutdown() {
    executor.shutdownNow();
  }

  @Override
  public Map<MetricCode, Quantity<?>> getMetrics(NetworkInterfaceUID interfaceId) {
    String iface = interfaceId.getName();
    if (!statistics.containsKey(iface)) {
      return Collections.emptyMap();
    }

    return statistics.get(iface);
  }

  public static void main(String[] args) throws Exception {
    long sampleTimeMs = 10_000;
    LinuxNetworkMetricsSupplier supplier = new LinuxNetworkMetricsSupplier(new PlainSysfsReader().narrow("class", "net"),
      Executors.newSingleThreadScheduledExecutor(), sampleTimeMs
    );

    while (true) {
      try {
        Thread.sleep(sampleTimeMs);
      } catch (InterruptedException e) {
        break;
      }

      long time = System.currentTimeMillis();
      System.out.println( time + "\n---");
      Map<MetricCode, Quantity<?>> metrics = supplier.getMetrics(new NetworkInterfaceUID("eth", "ens2f1"));

      for (Entry<MetricCode, Quantity<?>> entry : metrics.entrySet()) {
        MetricCode k = entry.getKey();
        Quantity<?> v = entry.getValue();
        if (v.getValue().intValue() != 0 || k.getCode().contains("bandwidth")) {
          System.out.println(k.getCode() + "=" + v);
        }
      }
      System.out.println("---");
    }

    System.in.read();
  }
}
