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
package org.connectorio.plc4x.decorator.phase;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhaseDecorator implements ReadDecorator, WriteDecorator, SubscribeDecorator, UnsubscribeDecorator {

  private final Logger logger = LoggerFactory.getLogger(PhaseDecorator.class);

  @Override
  public Builder decorateRead(Builder delegate) {
    return new DecoratorReadBuilder(delegate, this);
  }

  @Override
  public PlcReadRequest decorateReadRequest(PlcReadRequest delegate) {
    Optional<Phase> phase = Phase.get();
    if (!phase.isPresent()) {
      return delegate;
    }

    return new DecoratorReadRequest(delegate, this) {
      @Override
      public CompletableFuture<? extends PlcReadResponse> execute() {
        phase.ifPresent(Phase::register);
        CompletableFuture<PlcReadResponse> answer = new CompletableFuture<>();
        CompletableFuture<? extends PlcReadResponse> delegate = super.execute();
        delegate.whenComplete(new PhaseCallback<>(phase.orElse(null), answer));
        return answer;
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
    Optional<Phase> phase = Phase.get();
    if (!phase.isPresent()) {
      return delegate;
    }

    return new DecoratorWriteRequest(delegate, this) {
      @Override
      public CompletableFuture<? extends PlcWriteResponse> execute() {
        phase.ifPresent(Phase::register);
        CompletableFuture<PlcWriteResponse> answer = new CompletableFuture<>();
        super.execute().whenComplete(new PhaseCallback<>(phase.orElse(null), answer));
        return answer;
      }
    };
  }

  @Override
  public CompletableFuture<? extends PlcWriteResponse> decorateWriteResponse(DecoratorWriteRequest request, CompletableFuture<? extends PlcWriteResponse> response) {
    return response;
  }

  @Override
  public PlcSubscriptionRequest.Builder decorateSubscribe(PlcSubscriptionRequest.Builder delegate) {
    return new DecoratorSubscriptionRequestBuilder(delegate, this);
  }

  @Override
  public PlcSubscriptionRequest decorateSubscribeRequest(PlcSubscriptionRequest delegate) {
    Optional<Phase> phase = Phase.get();
    if (!phase.isPresent()) {
      return delegate;
    }

    return new DecoratorSubscriptionRequest(delegate, this) {
      @Override
      public CompletableFuture<? extends PlcSubscriptionResponse> execute() {
        phase.ifPresent(Phase::register);
        CompletableFuture<PlcSubscriptionResponse> answer = new CompletableFuture<>();
        super.execute().whenComplete(new PhaseCallback<>(phase.orElse(null), answer));
        return answer;
      }
    };
  }

  @Override
  public CompletableFuture<? extends PlcSubscriptionResponse> decorateSubscribeResponse(DecoratorSubscriptionRequest request, CompletableFuture<? extends PlcSubscriptionResponse> response) {
    return response;
  }

  @Override
  public PlcUnsubscriptionRequest.Builder decorateUnsubscribe(PlcUnsubscriptionRequest.Builder delegate) {
    return new DecoratorUnsubscriptionRequestBuilder(delegate, this);
  }

  @Override
  public PlcUnsubscriptionRequest decorateUnsubscribeRequest(PlcUnsubscriptionRequest delegate) {
    Optional<Phase> phase = Phase.get();
    if (!phase.isPresent()) {
      return delegate;
    }

    return new DecoratorUnsubscriptionRequest(delegate, this) {
      @Override
      public CompletableFuture<PlcUnsubscriptionResponse> execute() {
        phase.ifPresent(Phase::register);
        CompletableFuture<PlcUnsubscriptionResponse> answer = new CompletableFuture<>();
        super.execute().whenComplete(new PhaseCallback<>(phase.orElse(null), answer));
        return answer;
      }
    };
  }

  @Override
  public CompletableFuture<PlcUnsubscriptionResponse> decorateUnsubscribeResponse(PlcUnsubscriptionRequest request, CompletableFuture<PlcUnsubscriptionResponse> response) {
    return response;
  }

  // an universal callback which notify phase about completion of task.
  static class PhaseCallback<T> implements BiConsumer<T, Throwable> {

    private final Phase phase;
    private final CompletableFuture<T> answer;

    public PhaseCallback(Phase phase, CompletableFuture<T> answer) {
      this.phase = phase;
      this.answer = answer;
    }

    @Override
    public void accept(T response, Throwable error) {
      if (error != null) {
        answer.completeExceptionally(error);
      } else {
        answer.complete(response);
      }

      if (phase != null) {
        phase.arrive();
      }
    }
  }

}
