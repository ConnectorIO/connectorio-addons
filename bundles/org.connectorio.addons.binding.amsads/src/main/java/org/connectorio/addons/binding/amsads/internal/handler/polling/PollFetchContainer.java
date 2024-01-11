/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.amsads.internal.handler.polling;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.plc4x.java.ads.tag.AdsTag;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadRequest.Builder;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollFetchContainer implements FetchContainer {

  private final ScheduledExecutorService executor;
  private final PlcConnection connection;
  Map<Long, Map<String, Operation>> operations = new ConcurrentHashMap<>();
  Map<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

  public PollFetchContainer(ScheduledExecutorService executor, PlcConnection connection) {
    this.executor = executor;
    this.connection = connection;
  }

  @Override
  public void add(Long interval, String channelId, AdsTag tag, Consumer<Object> onChange) {
    if (!operations.containsKey(interval)) {
      operations.put(interval, new HashMap<>());
    }
    operations.get(interval).put(channelId, new Operation(tag, onChange));
  }

  @Override
  public boolean start() {
    if (operations.isEmpty()) {
      return false;
    }

    for (Entry<Long, Map<String, Operation>> operation : operations.entrySet()) {
      Builder requestBuilder = connection.readRequestBuilder();
      Map<String, Operation> polledValues = operation.getValue();
      for (Entry<String, Operation> polledValue : polledValues.entrySet()) {
        requestBuilder.addTag(polledValue.getKey(), polledValue.getValue().tag);
      }

      ScheduledFuture<?> future = executor.scheduleAtFixedRate(new PollingRunnable(requestBuilder.build(), polledValues),
        operation.getKey(), operation.getKey(), TimeUnit.MILLISECONDS);
      futures.put(operation.getKey(), future);
    }

    return true;
  }

  @Override
  public void stop() {
    futures.forEach((key, future) -> future.cancel(true));
  }

  static class Operation {

    private final AdsTag tag;
    private final Consumer<Object> onChange;

    public Operation(AdsTag tag, Consumer<Object> onChange) {
      this.tag = tag;
      this.onChange = onChange;
    }

    public void onChange(Object object) {
      onChange.accept(object);
    }
  }

  static class PollingRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(PollingRunnable.class);

    private final PlcReadRequest request;
    private final Map<String, Operation> polledValues;

    PollingRunnable(PlcReadRequest request, Map<String, Operation> polledValues) {
      this.request = request;
      this.polledValues = polledValues;
    }

    @Override
    public void run() {
      logger.debug("Fetching data for channels {}", polledValues.keySet());
      request.execute().whenComplete((result, error) -> {
        if (error != null) {
          logger.warn("Failed to poll data through cyclic read request", error);
          return;
        }

        for (String tag : result.getTagNames()) {
          PlcResponseCode responseCode = result.getResponseCode(tag);
          if (polledValues.containsKey(tag) && responseCode == PlcResponseCode.OK) {
            polledValues.get(tag).onChange(result.getObject(tag));
          } else {
            logger.warn("Unexpected response code {} for channel {}", responseCode, tag);
          }
        }
      });
    }
  }
}
