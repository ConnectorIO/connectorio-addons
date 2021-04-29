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
package org.connectorio.plc4x.decorator.throttle;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadRequest.Builder;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.connectorio.plc4x.decorator.DecoratorReadBuilder;
import org.connectorio.plc4x.decorator.DecoratorReadRequest;
import org.connectorio.plc4x.decorator.DecoratorSubscriptionRequest;
import org.connectorio.plc4x.decorator.DecoratorSubscriptionRequestBuilder;
import org.connectorio.plc4x.decorator.DecoratorUnsubscriptionRequest;
import org.connectorio.plc4x.decorator.DecoratorUnsubscriptionRequestBuilder;
import org.connectorio.plc4x.decorator.DecoratorWriteBuilder;
import org.connectorio.plc4x.decorator.DecoratorWriteRequest;
import org.connectorio.plc4x.decorator.ReadDecorator;
import org.connectorio.plc4x.decorator.SubscribeDecorator;
import org.connectorio.plc4x.decorator.UnsubscribeDecorator;
import org.connectorio.plc4x.decorator.WriteDecorator;

public class ThrottleDecorator implements ReadDecorator, WriteDecorator {

  private final RateLimiter readLimit;
  private final RateLimiter writeLimit;

  public ThrottleDecorator(double rate) {
    this(rate, rate);
  }

  @SuppressWarnings("all")
  public ThrottleDecorator(double readRate, double writeRate) {
    this.readLimit = RateLimiter.create(readRate, 0, TimeUnit.SECONDS);
    this.writeLimit = RateLimiter.create(writeRate, 0, TimeUnit.SECONDS);
  }

  @Override
  public Builder decorateRead(Builder delegate) {
    return new DecoratorReadBuilder(delegate, this);
  }

  @Override
  public PlcReadRequest decorateReadRequest(PlcReadRequest delegate) {
    return new DecoratorReadRequest(delegate, this) {
      @Override
      public CompletableFuture<? extends PlcReadResponse> execute() {
        readLimit.acquire();
        return delegate.execute();
      }
    };
  }

  @Override
  public CompletableFuture<? extends PlcReadResponse> decorateReadResponse(DecoratorReadRequest request, CompletableFuture<? extends PlcReadResponse> response) {
    return response;
  }

  @Override
  public PlcWriteRequest.Builder decorateWrite(PlcWriteRequest.Builder delegate) {
    return new DecoratorWriteBuilder(delegate, this);
  }

  @Override
  public PlcWriteRequest decorateWriteRequest(PlcWriteRequest delegate) {
    return new DecoratorWriteRequest(delegate, this) {
      @Override
      public CompletableFuture<? extends PlcWriteResponse> execute() {
        writeLimit.acquire();
        return delegate.execute();
      }
    };
  }

  @Override
  public CompletableFuture<? extends PlcWriteResponse> decorateWriteResponse(DecoratorWriteRequest request, CompletableFuture<? extends PlcWriteResponse> response) {
    return response;
  }

}
