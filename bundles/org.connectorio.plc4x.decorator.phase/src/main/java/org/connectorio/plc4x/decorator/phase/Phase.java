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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Phase {

  private final static Timer completionTimer = new Timer("phase-completer");
  private final static AtomicReference<Phase> PHASE = new AtomicReference<>();
  private final static Semaphore LOCK = new Semaphore(1);
  private final Logger logger = LoggerFactory.getLogger(Phase.class);

  private final AtomicInteger counter = new AtomicInteger();
  private final List<Runnable> callbacks = new ArrayList<>();
  private final String label;
  private final long closeTime;

  Phase(String label, long closeTime) {
    this.label = label;
    this.closeTime = closeTime;
    logger.debug("Created new phase {}. Close time: {}", label, closeTime);
  }

  public static Phase create(String label) {
    return create(label, 5_000);
  }

  public static Phase create(String label, int closeTime) {
    LOCK.acquireUninterruptibly();
    Phase phase = new Phase(label, closeTime);
    PHASE.set(phase);
    return phase;
  }

  static Optional<Phase> get() {
    return Optional.ofNullable(PHASE.get());
  }

  public void addCallback(Runnable runnable) {
    callbacks.add(runnable);
    logger.debug("Phase {}, registered completion callback {}", label, runnable);
  }

  int register() {
    int counter = this.counter.incrementAndGet();
    logger.debug("Registered a new task in phase {}. Current balance: {}", label, counter);
    return counter;
  }

  void arrive() {
    logger.debug("Arrival of task in phase {}. Current balance: {}. Scheduling close of phase.", label, counter.get());
    completionTimer.schedule(new CompletionTimer(this), closeTime);
  }

  void complete() {
    callbacks.forEach(Runnable::run);
  }

  public String toString() {
    return "Phase [" + label + "] tasks: " + counter;
  }

  static class CompletionTimer extends TimerTask {

    private final Logger logger = LoggerFactory.getLogger(CompletionTimer.class);
    private final Phase phase;

    public CompletionTimer(Phase phase) {
      this.phase = phase;
    }

    @Override
    public void run() {
      if (phase.counter.decrementAndGet() == 0) {
        logger.debug("All phase {} tasks completed. Calling completion.", phase.label);
        try {
          phase.complete();
          logger.debug("Phase {} closed successfully.", phase.label);
        } catch (Exception e) {
          logger.error("Phase {} completion or one of its callback reported an error", phase.label, e);
        } finally {
          PHASE.set(null);
          LOCK.release();
        }
        return;
      }
      logger.debug("Phase {} not completed yet. Current balance: {}.", phase.label, phase.counter.get());
    }
  }

}
