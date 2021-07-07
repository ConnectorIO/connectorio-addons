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

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhaseLock {

  private final static Logger LOGGER = LoggerFactory.getLogger(PhaseLock.class);

  private final static AtomicReference<Phase> PHASE = new AtomicReference<>();
  private final static Semaphore LOCK = new Semaphore(1);

  static Optional<Phase> get() {
    Phase phase = PHASE.get();
    LOGGER.debug("Inquiry of current phase. Result: {}", phase);
    return Optional.ofNullable(phase);
  }

  static void set(Phase phase) {
    Phase current = PHASE.get();
    if (phase.equals(current)) {
      LOGGER.debug("Request in active phase {}", current);
      return;
    }

    LOGGER.debug("Blocking request from {} until earlier phase complete", phase);
    LOCK.acquireUninterruptibly();

    if (phase.isDone()) {
      LOCK.release();
      return;
    }

    PHASE.set(phase);
    LOGGER.debug("Changed phase to {}", phase);
    phase.start();
  }

  static void release(Phase phase) {
    LOGGER.debug("Closed phase {}", phase);
    PHASE.set(null);
    LOCK.release();
  }
}
