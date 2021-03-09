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
package org.connectorio.plc4x.decorator;

import java.util.Collection;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest.Builder;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;

public class DecoratorUnsubscriptionRequestBuilder implements Builder {

  private final Builder delegate;
  private final UnsubscribeDecorator decorator;

  public DecoratorUnsubscriptionRequestBuilder(Builder delegate, UnsubscribeDecorator decorator) {
    this.delegate = delegate;
    this.decorator = decorator;
  }

  @Override
  public PlcUnsubscriptionRequest build() {
    return new DecoratorUnsubscriptionRequest(delegate.build(), decorator);
  }

  @Override
  public Builder addHandles(PlcSubscriptionHandle plcSubscriptionHandle) {
    delegate.addHandles(plcSubscriptionHandle);
    return this;
  }

  @Override
  public Builder addHandles(PlcSubscriptionHandle plcSubscriptionHandle1, PlcSubscriptionHandle... plcSubscriptionHandles) {
    delegate.addHandles(plcSubscriptionHandle1, plcSubscriptionHandles);
    return this;
  }

  @Override
  public Builder addHandles(Collection<PlcSubscriptionHandle> plcSubscriptionHandle) {
    delegate.addHandles(plcSubscriptionHandle);
    return this;
  }

}
