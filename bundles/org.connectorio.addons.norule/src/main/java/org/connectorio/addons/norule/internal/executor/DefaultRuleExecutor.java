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
package org.connectorio.addons.norule.internal.executor;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.connectorio.addons.norule.RuleExecutor;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic wrapper for scheduled executor service which tracks active units.
 */
@Component
public class DefaultRuleExecutor implements RuleExecutor {


  private final Logger logger = LoggerFactory.getLogger(DefaultRuleExecutor.class);
  private final AtomicInteger threadId = new AtomicInteger();
  private final AtomicInteger counter = new AtomicInteger();
  private final AtomicLong executions = new AtomicLong();
  private final AtomicLong failures = new AtomicLong();

  private final ScheduledExecutorService executor;

  public DefaultRuleExecutor() {
    executor = Executors.newScheduledThreadPool(calculatePoolSize(), runnable -> {
      Thread thread = new Thread(new ExecutionWrapper(runnable, executions, counter), "rule-execution-" + threadId.getAndIncrement());
      thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread parent, Throwable error) {
          failures.incrementAndGet();
          logger.error("Error while running task", error);
        }
      });
      return thread;
    });
  }

  @Override
  public int getActivateExecutions() {
    return counter.get();
  }

  @Override
  public long getExecutionsCounter() {
    return executions.get();
  }

  @Override
  public long getFailuresCounter() {
    return failures.get();
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public void execute(Runnable runnable) {
    executor.execute(runnable);
  }

  @Override
  public void submit(Runnable runnable) {
    executor.submit(runnable);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit delayUnit) {
    return executor.scheduleAtFixedRate(runnable, initialDelay, period, delayUnit);
  }

  private static int calculatePoolSize() {
    int processors = Runtime.getRuntime().availableProcessors();
    if (processors == 1) {
      return 1;
    }
    return processors / 2;
  }

}
