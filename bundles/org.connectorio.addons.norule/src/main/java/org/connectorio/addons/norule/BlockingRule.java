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
package org.connectorio.addons.norule;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Rule which signals its execution via latch.
 */
public class BlockingRule implements Rule {

  private final CountDownLatch latch;
  private final Rule rule;

  public BlockingRule(Rule rule) {
    this(new CountDownLatch(1), rule);
  }

  public BlockingRule(CountDownLatch latch, Rule rule) {
    this.latch = latch;
    this.rule = rule;
  }

  @Override
  public Set<Trigger> getTriggers() {
    return rule.getTriggers();
  }

  @Override
  public Set<Condition> getConditions() {
    return rule.getConditions();
  }

  @Override
  public void handle(RuleContext context) {
    try {
      rule.handle(context);
    } finally {
      latch.countDown();
    }
  }

  @Override
  public RuleUID getUID() {
    return rule.getUID();
  }

  public CountDownLatch getLatch() {
    return latch;
  }

  public boolean await() throws InterruptedException {
    return await(5, TimeUnit.SECONDS);
  }

  public boolean await(int amount, TimeUnit unit) throws InterruptedException {
    return latch.await(amount, unit);
  }

  public String toString() {
    return "Blocking Rule [" + rule + "]";
  }

}
