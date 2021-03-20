/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.internal.plc4x;

import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.connectorio.addons.binding.plc4x.canopen.api.CoSubscription;

public class DefaultSubscription<T> implements CoSubscription {

  private final PlcConnection connection;
  private final PlcSubscriptionHandle subscribe;
  private final Consumer<T> consumer;
  private final PlcConsumerRegistration registration;

  public DefaultSubscription(PlcConnection connection, PlcSubscriptionHandle subscribe, Consumer<T> consumer, Function<PlcReadResponse, T> extractor) {
    this.connection = connection;
    this.subscribe = subscribe;
    this.consumer = consumer;
    registration = subscribe.register(new Consumer<PlcSubscriptionEvent>() {
      @Override
      public void accept(PlcSubscriptionEvent event) {
        consumer.accept(extractor.apply(event));
      }
    });
  }

  @Override
  public void close() throws Exception {
    unsubscribe();
  }

  @Override
  public void unsubscribe() {
    registration.unregister();
  }

  public String toString() {
    return "CoSubscription [" + subscribe + "]";
  }

}
