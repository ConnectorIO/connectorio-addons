package org.connectorio.addons.communication.watchdog.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.communication.watchdog.Watchdog;
import org.connectorio.addons.communication.watchdog.WatchdogBuilder;
import org.connectorio.addons.communication.watchdog.WatchdogClock;
import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.connectorio.addons.communication.watchdog.WatchdogCondition.State;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogInitializedEvent;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogRecoveryEvent;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogTimeoutEvent;
import org.connectorio.addons.communication.watchdog.WatchdogListener;
import org.connectorio.addons.communication.watchdog.WatchdogManager;
import org.openhab.core.thing.Thing;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultWatchdogManager implements WatchdogManager {

  private final Logger logger = LoggerFactory.getLogger(DefaultWatchdogManager.class);

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
    @Override
    public Thread newThread(Runnable runnable) {
      return new Thread(runnable, "co7io-communication-watchdog");
    }
  });

  private final Map<Thing, Watchdog> watchdogMap = new HashMap<>();
  private final Map<Watchdog, List<ScheduledFuture>> checks = new HashMap<>();

  private final WatchdogClock clock;

  public DefaultWatchdogManager() {
    this(new DefaultWatchdogClock());
  }

  public DefaultWatchdogManager(WatchdogClock clock) {
    this.clock = clock;
  }

  @Override
  public WatchdogBuilder builder(Thing thing) {
    return new DefaultWatchdogBuilder(clock, this, thing);
  }

  public void destroy() {
    for (Watchdog watchdog : watchdogMap.values()) {
      closeWatchdog(watchdog);
    }
  }

  void registerWatchdog(Thing thing, DefaultWatchdog watchdog, WatchdogListener listener) {
    Watchdog previous = watchdogMap.put(thing, watchdog);
    if (previous != null) {
      closeWatchdog(previous);
    }

    Map<WatchdogCondition, Long> timeoutChecks = watchdog.getConditionIntervalMap();
    for (Entry<WatchdogCondition, Long> entry : timeoutChecks.entrySet()) {
      ScheduledFuture<?> future = executor.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          WatchdogCondition condition = entry.getKey();
          evaluateCondition(condition, listener);
        }
      }, entry.getValue(), entry.getValue(), TimeUnit.MILLISECONDS);
      if (!checks.containsKey(watchdog)) {
        checks.put(watchdog, new ArrayList<>());
      }
      checks.get(watchdog).add(future);
    }
  }

  void close(Watchdog watchdog) {
    if (watchdogMap.containsKey(watchdog)) {
      closeWatchdog(watchdog);
      watchdogMap.remove(watchdog);
    }
  }

  private void closeWatchdog(Watchdog watchdog) {
    List<ScheduledFuture> futures = checks.get(watchdog);
    if (futures != null && !futures.isEmpty()) {
      for (ScheduledFuture<?> future : futures) {
        future.cancel(true);
      }
    }
    // dereference old checks / futures
    checks.remove(watchdog);
  }

  private void evaluateCondition(WatchdogCondition condition, WatchdogListener listener) {
    State pastState = condition.getState();
    State currentState = condition.evaluate();
    logger.debug("Evaluation of condition {} with listener {}. Past state {}, current state {}", condition, listener, pastState, currentState);
    if (currentState == State.INITIALIZATION) {
      if (State.INITIALIZATION != pastState) {
        logger.debug("Condition {} is in initialization state", condition);
        listener.initialized(new WatchdogInitializedEvent(condition.getChannel()));
      }
    } else if (currentState == State.OK) {
      if (pastState != State.OK) {
        logger.debug("Condition {} switched to OK state", condition);
        listener.recovery(new WatchdogRecoveryEvent(condition.getChannel()));
      }
    } else if (currentState == State.FAILURE) {
      if (pastState != State.FAILURE) {
        logger.debug("Condition {} switched to FAILURE state", condition);
        listener.timeout(new WatchdogTimeoutEvent(condition.getChannel()));
      }
    }
  }

  void check(Watchdog watchdog, WatchdogListener listener) {
    for (Entry<WatchdogCondition, Long> entry : watchdog.getConditionIntervalMap().entrySet()) {
      evaluateCondition(entry.getKey(), listener);
    }
  }
}
