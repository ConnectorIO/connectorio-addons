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
