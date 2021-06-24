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
package org.connectorio.plc4x.decorator.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.plc4x.java.api.messages.PlcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Phase {

  private final Timer timer;

  private final Logger logger = LoggerFactory.getLogger(Phase.class);
  private final List<Runnable> start = new ArrayList<>();
  private final List<Runnable> completions = new ArrayList<>();
  private final List<Runnable> errors = new ArrayList<>();
  private final AtomicLong active = new AtomicLong();
  private final AtomicLong completed = new AtomicLong();

  private final String label;
  private final long closeTime;
  private final AtomicBoolean done = new AtomicBoolean();
  private final AtomicBoolean error = new AtomicBoolean();

  public Phase(String label) {
    this(label, 5_000);
  }

  public Phase(String label, long closeTime) {
    this.label = label;
    this.closeTime = closeTime;
    this.timer = new Timer("phase-completer-" + label);
    logger.debug("Created new phase '{}'. Close time: {}", label, closeTime);
  }

  public void start() {
    logger.debug("Phase {} become active, starting completion timer.", label);
    start.forEach(Runnable::run);
    timer.schedule(new CompletionTimer(timer, this, Long.MAX_VALUE, 0), closeTime);
  }

  public void onStart(Runnable runnable) {
    start.add(runnable);
  }

  public void onError(Runnable runnable) {
    errors.add(runnable);
  }

  public void onCompletion(Runnable runnable) {
    completions.add(runnable);
    logger.debug("Phase '{}', registered completion callback '{}'", label, runnable);
  }

  void register(PlcRequest request) {
    active.incrementAndGet();
  }

  void arrive(PlcRequest request) {
    active.decrementAndGet();
    completed.incrementAndGet();
  }

  void complete() {
    done.set(true);
    completions.forEach(Runnable::run);
  }

  void error() {
    error.set(true);
    errors.forEach(Runnable::run);
  }

  boolean isDone() {
    return done.get();
  }

  boolean isError() {
    return error.get();
  }

  public String toString() {
    return "Phase [" + label + ":" + active + "/" + completed + " (done: " + isDone() + ", error: " + isError() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Phase)) {
      return false;
    }
    Phase phase = (Phase) o;
    return Objects.equals(label, phase.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label);
  }

  static class CompletionTimer extends TimerTask {

    private final Logger logger = LoggerFactory.getLogger(CompletionTimer.class);
    private final Timer timer;
    private final Phase phase;
    private final long previousCount;
    private final int freezeCount;
    private final boolean complete;

    public CompletionTimer(Timer timer, Phase phase, long previousCount, int freezeCount) {
      this(timer, phase, previousCount, freezeCount, false);
    }

    public CompletionTimer(Timer timer, Phase phase, long previousCount, int freezeCount, boolean complete) {
      this.timer = timer;
      this.phase = phase;
      this.previousCount = previousCount;
      this.freezeCount = freezeCount;
      this.complete = complete;
    }

    @Override
    public void run() {
      long taskCount = phase.active.get();

      int freezeCounter = 0;
      if (taskCount == previousCount) {
        freezeCounter = freezeCount + 1;
        logger.debug("Detected a deadlock in phase '{}'. Amount of tasks is frozen at {}. This is {} freeze cycle.", phase.label, taskCount, freezeCount);
      }

      if (taskCount != 0) {
        logger.debug("Phase '{}' has {} tasks awaiting completion.", phase.label, phase.active.get());
        if (freezeCounter == 10) {
          logger.debug("Phase '{}' been waiting for completion too long, forcing its closure.", phase.label);
          // terminate
          close(phase::error, "timed out");
          return;
        }
        timer.schedule(new CompletionTimer(timer, phase, taskCount, freezeCounter), phase.closeTime);
        return;
      }

      if (!complete) {
        logger.debug("Phase '{}' tasks completed, awaiting {} to close phase.", phase.label, phase.closeTime);
        timer.schedule(new CompletionTimer(timer, phase, previousCount, freezeCounter,true), phase.closeTime);
        return;
      }

      close(phase::complete, "completed");
    }

    private void close(Runnable closure, final String completionReason) {
      logger.debug("Phase '{}' " + completionReason + ". Executing appropriate closure tasks.", phase.label);
      try {
        timer.cancel();
        closure.run();
        logger.debug("Phase '{}' closed successfully.", phase.label);
      } catch (Exception e) {
        logger.error("Phase '{}' closure or one of its callback reported an error", phase.label, e);
      } finally {
        PhaseLock.release(phase);
      }
    }
  }

}
