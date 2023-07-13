/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.canopen.handler;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.spi.values.PlcStruct;
import org.connectorio.addons.binding.canopen.api.CoConnection;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.api.CoSubscription;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of basic CANopen heartbeat listener which attempt to detect communication failures.
 *
 * Detection algorithm is based on basic timeout check.
 */
public class HeartbeatMonitor implements Runnable, Consumer<PlcStruct> {

  private final Logger logger = LoggerFactory.getLogger(HeartbeatMonitor.class);
  private final AtomicLong lastSeen = new AtomicLong();
  private final Clock clock;
  private final CoNode node;
  private final HeartbeatCallback callback;
  private final long timeoutMs;
  private CoSubscription subscription;


  public HeartbeatMonitor(CoConnection connection, CoNode node, HeartbeatCallback callback, long lastSeen, long timeoutMs) {
    this(Clock.systemUTC(), connection, node, callback, lastSeen, timeoutMs);
  }

  HeartbeatMonitor(Clock clock, CoConnection connection, CoNode node, HeartbeatCallback callback, long lastSeen, long timeoutMs) {
    this.clock = clock;
    this.node = node;
    this.callback = callback;
    this.timeoutMs = timeoutMs;

    connection.heartbeat(node.getNodeId(), this).whenComplete((subscription, error) -> {
      if (error != null) {
        logger.error("Failed to listen for heartbeat information", error);
        return;
      }
      this.lastSeen.set(lastSeen);
      this.subscription = subscription;
    });
  }

  @Override
  public void run() {
    if (clock.millis() >= lastSeen.get() + timeoutMs) {
      callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Heartbeat not received within configured timeout");
    }
  }

  public void close() throws Exception {
    if (subscription != null) {
      subscription.close();
    }
  }

  @Override
  public void accept(PlcStruct heartbeat) {
    final Map<String, ? extends PlcValue> struct = heartbeat.getStruct();
    final Integer nodeId = Optional.ofNullable(struct.get("node")).map(PlcValue::getInt).orElse(0);
    final Integer state = Optional.ofNullable(struct.get("state")).map(PlcValue::getInt).orElse(0);

    if (node.getNodeId() != nodeId) {
      return;
    }

    lastSeen.set(clock.millis());
    if (0x00 == state) {
      callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "Booting up");
    } else if (0x04 == state) {
      callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "Node stopped");
    } else if (0x05 == state) {
      callback.updateStatus(ThingStatus.ONLINE);
    } else if (0x7F == state) {
      callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "Node is in pre-operational state");
    } else {
      logger.info("Unsupported CANopen state {}(0x{}) received for node {}, assuming offline state", state, Integer.toHexString(state), node);
      callback.updateStatus(ThingStatus.OFFLINE);
    }
  }

}
