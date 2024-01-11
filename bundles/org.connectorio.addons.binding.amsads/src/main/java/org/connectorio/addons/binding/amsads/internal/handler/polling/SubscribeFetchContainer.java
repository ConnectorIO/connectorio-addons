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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.apache.plc4x.java.ads.tag.AdsTag;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeFetchContainer implements FetchContainer {

  private final Logger logger = LoggerFactory.getLogger(SubscribeFetchContainer.class);

  private final Map<String, Consumer<Object>> handlers = new ConcurrentHashMap<>();
  private final PlcSubscriptionRequest.Builder subscribeBuilder;
  private final PlcUnsubscriptionRequest.Builder unsubscribeBuilder;


  public SubscribeFetchContainer(PlcConnection connection) {
    subscribeBuilder = connection.subscriptionRequestBuilder();
    unsubscribeBuilder = connection.unsubscriptionRequestBuilder();
  }

  @Override
  public void add(Long interval, String channelId, AdsTag tag, Consumer<Object> onChange) {
    subscribeBuilder.addChangeOfStateTag(channelId, tag);
    handlers.put(channelId, onChange);
  }

  @Override
  public boolean start() {
    if (handlers.isEmpty()) {
      return false;
    }

    subscribeBuilder.build().execute().whenComplete((response, error) -> {
      if (error != null) {
        return;
      }

      for (String channelId : response.getTagNames()) {
        PlcSubscriptionHandle subscriptionHandle = response.getSubscriptionHandle(channelId);
        subscriptionHandle.register(new Consumer<PlcSubscriptionEvent>() {
          @Override
          public void accept(PlcSubscriptionEvent plcSubscriptionEvent) {
            Object value = plcSubscriptionEvent.getObject(channelId);
            if (value != null) {
              logger.debug("Channel {} received update {}", channelId, value);
              Consumer<Object> consumer = handlers.get(channelId);
              if (consumer != null) {
                consumer.accept(value);
              } else {
                logger.warn("Unknown channel/tag association: {}", channelId);
              }
            }
          }
        });
        unsubscribeBuilder.addHandles(subscriptionHandle);
      }
    });
    return true;
  }

  @Override
  public void stop() {
    unsubscribeBuilder.build().execute().join();
  }

}
