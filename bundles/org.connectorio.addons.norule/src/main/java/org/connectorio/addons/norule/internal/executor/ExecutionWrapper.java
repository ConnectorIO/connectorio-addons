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

import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Runnable which tracks executions.
 */
class ExecutionWrapper implements Runnable {

  private final Runnable delegate;
  private final AtomicLong executions;
  private final Set<String> active;

  ExecutionWrapper(Runnable delegate, AtomicLong executions, Set<String> active) {
    this.delegate = delegate;
    this.executions = executions;
    this.active = active;
  }

  @Override
  public void run() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    String rule = delegate.toString();
    try {
      Thread.currentThread().setContextClassLoader(delegate.getClass().getClassLoader());
      active.add(rule);
      delegate.run();
    } finally {
      active.remove(rule);
      executions.incrementAndGet();
      Thread.currentThread().setContextClassLoader(loader);
    }
  }

}