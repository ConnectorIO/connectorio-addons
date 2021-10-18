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
package org.connectorio.addons.norule.internal;

import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import org.connectorio.addons.norule.Periodic;
import org.connectorio.addons.norule.RuleUID;
import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.RuleContext;
import org.connectorio.addons.norule.RuleProvider;
import org.connectorio.addons.norule.RuleRegistry;
import org.connectorio.addons.norule.Scheduled;
import org.connectorio.addons.norule.SkipExecutionException;
import org.connectorio.addons.norule.Trigger;
import org.connectorio.addons.norule.internal.context.EmptyTriggerRuleContext;
import org.connectorio.addons.norule.internal.context.ItemStateContext;
import org.connectorio.addons.norule.internal.context.ReadyMarkerAddedRuleContext;
import org.connectorio.addons.norule.internal.context.ReadyMarkerRemovedRuleContext;
import org.connectorio.addons.norule.internal.context.ScheduledRuleContext;
import org.connectorio.addons.norule.internal.context.StartLevelRuleContext;
import org.connectorio.addons.norule.internal.trigger.EmptyTrigger;
import org.connectorio.addons.norule.internal.trigger.GroupStateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerAddedTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerRemovedTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerTrigger;
import org.connectorio.addons.norule.internal.trigger.StartLevelTrigger;
import org.connectorio.addons.norule.internal.trigger.StateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.StateUpdateTrigger;
import org.connectorio.chrono.Period;
import org.connectorio.chrono.shared.FuturePeriodCalculator;
import org.connectorio.chrono.shared.PastPeriodCalculator;
import org.openhab.core.common.registry.AbstractRegistry;
import org.openhab.core.common.registry.ManagedProvider;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.events.system.StartlevelEvent;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.GroupItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {EventSubscriber.class, RuleRegistry.class, ReadyTracker.class})
public class NoRuleRegistry extends AbstractRegistry<Rule, RuleUID, RuleProvider> implements EventSubscriber, RuleRegistry, ReadyTracker  {

  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, runnable -> {
    Thread thread = new Thread(runnable, "rule-execution");
    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        LoggerFactory.getLogger(NoRuleRegistry.class).error("Error while running task", e);
      }
    });
    return thread;
  });
  private final Logger logger = LoggerFactory.getLogger(NoRuleRegistry.class);
  private final Map<Trigger, Future<?>> scheduledRules = new ConcurrentHashMap<>();
  private final Map<ReadyMarker, List<Rule>> readyMarkerRules = new ConcurrentHashMap<>();
  private final ItemRegistry itemRegistry;
  private final ReadyService readyService;
  private final ThingsActionsRegistry actionsRegistry;
  private final AtomicInteger startLevel = new AtomicInteger();

  @Activate
  public NoRuleRegistry(@Reference ItemRegistry itemRegistry, @Reference ReadyService readyService, @Reference ThingsActionsRegistry actionsRegistry) {
    super(RuleProvider.class);
    this.itemRegistry = itemRegistry;
    this.readyService = readyService;
    this.actionsRegistry = actionsRegistry;
    readyService.registerTracker(this, null);
  }

  @Deactivate
  void shutdown() {
    readyService.unregisterTracker(this);
    executor.shutdown();
  }

  @Override
  public void run(RuleUID ruleUID) {
    Rule rule = get(ruleUID);
    if (rule != null) {
      Scheduled schedule = null;
      for (Trigger trigger : rule.getTriggers()) {
        if (trigger instanceof Scheduled) {
          schedule = (Scheduled) trigger;
          break;
        }
      }
      if (schedule != null) {
        executor.execute(new ScheduledRunnable(itemRegistry, actionsRegistry, rule, schedule));
      } else {
        executor.execute(new RuleRunnable(rule, new EmptyTriggerRuleContext(itemRegistry, actionsRegistry, new EmptyTrigger())));
      }
    }
  }

  @Override
  public Set<String> getSubscribedEventTypes() {
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      GroupItemStateChangedEvent.TYPE,
      ItemStateEvent.TYPE,
      ItemStateChangedEvent.TYPE,
      StartlevelEvent.TYPE
    )));
  }

  @Override
  public EventFilter getEventFilter() {
    return null;
  }

  @Override
  public void receive(Event event) {
    if (event instanceof GroupItemStateChangedEvent) {
      GroupItemStateChangedEvent stateChangeEvent = (GroupItemStateChangedEvent) event;
      fire((trigger) -> new ItemStateContext(itemRegistry, actionsRegistry, trigger, stateChangeEvent.getItemName(), stateChangeEvent.getItemState()), (trigger) -> {
        return trigger instanceof GroupStateChangeTrigger && stateChangeEvent.getItemName().equals(((GroupStateChangeTrigger) trigger).getGroupName());
      });
    } else if (event instanceof ItemStateChangedEvent) {
      ItemStateChangedEvent stateChangeEvent = (ItemStateChangedEvent) event;
      fire((trigger) -> new ItemStateContext(itemRegistry, actionsRegistry, trigger, stateChangeEvent.getItemName(), stateChangeEvent.getItemState()), (trigger) -> {
        return trigger instanceof StateChangeTrigger && stateChangeEvent.getItemName().equals(((StateChangeTrigger) trigger).getItemName());
      });
    } else if (event instanceof ItemStateEvent) {
      ItemStateEvent stateEvent = (ItemStateEvent) event;
      fire((trigger) -> new ItemStateContext(itemRegistry, actionsRegistry, trigger, stateEvent.getItemName(), stateEvent.getItemState()), (trigger -> {
        return trigger instanceof StateUpdateTrigger && stateEvent.getItemName().equals(((StateUpdateTrigger) trigger).getItemName());
      }));
    } else if (event instanceof StartlevelEvent) {
      StartlevelEvent startlevelEvent = (StartlevelEvent) event;
      fire((trigger) -> new StartLevelRuleContext(itemRegistry, actionsRegistry, trigger, startLevel.get(), startlevelEvent.getStartlevel()), (trigger -> {
        return trigger instanceof StartLevelTrigger && startlevelEvent.getStartlevel() <= ((StartLevelTrigger) trigger).getStartLevel();
      }));
      startLevel.set(startlevelEvent.getStartlevel());
    } else {
      logger.debug("Unsupported event received {}", event);
    }
  }

  private final void fire(Function<Trigger, RuleContext> contextFactory, Predicate<Trigger> predicate) {
    fire(getAll(), contextFactory, predicate);
  }

  private final void fire(Collection<Rule> rules, Function<Trigger, RuleContext> contextFactory, Predicate<Trigger> predicate) {
    for (Rule rule : rules) {
      for (Trigger trigger : rule.getTriggers()) {
        if (predicate.test(trigger)) {
          logger.info("Rule {} is triggered by {}.", rule, trigger);
            executor.submit(new RuleRunnable(rule, contextFactory.apply(trigger)));
        }
      }
    }
  }

  void addProvider(RuleProvider provider) {
    if (provider instanceof ManagedProvider) {
      setManagedProvider((ManagedProvider<Rule, RuleUID>) provider);
    }
    super.addProvider(provider);
  }

  @Override
  protected void onAddElement(Rule element) throws IllegalArgumentException {
    super.onAddElement(element);

    for (Trigger trigger : element.getTriggers()) {
      if (trigger instanceof Scheduled) {
        Scheduled schedule = (Scheduled) trigger;
        TimeUnit delayUnit = TimeUnit.MILLISECONDS;
        long initialDelay = calculateDelay(schedule);
        logger.debug("First run of scheduled rule {} in {} ms ({} {}). Then every {} {}", element, initialDelay,
          schedule.getTimeUnit().convert(initialDelay, delayUnit), schedule.getTimeUnit(),
          schedule.getDelay(), schedule.getTimeUnit()
        );
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(new ScheduledRunnable(itemRegistry, actionsRegistry, element, schedule), initialDelay,
          delayUnit.convert(schedule.getDelay(), schedule.getTimeUnit()), delayUnit);
        scheduledRules.put(schedule, future);
      }
      if (trigger instanceof Periodic) {
        Periodic periodic = (Periodic) trigger;
        long initialDelay = calculateDelay(periodic);
        long milli = delay(periodic);
        logger.debug("First run of periodic rule {} in {} ms. Then every {} ms", element, initialDelay, milli);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(new ScheduledRunnable(itemRegistry, actionsRegistry, element, periodic), initialDelay,
          milli, TimeUnit.MILLISECONDS);
        scheduledRules.put(periodic, future);
      }
      if (trigger instanceof ReadyMarkerTrigger) {
        ReadyMarker marker = ((ReadyMarkerTrigger) trigger).getMarker();
        if (!readyMarkerRules.containsKey(marker)) {
          readyMarkerRules.put(marker, new ArrayList<>());
        }
        readyMarkerRules.get(marker).add(element);
      }
    }
  }

  @Override
  protected void onRemoveElement(Rule element) {
    super.onRemoveElement(element);

    for (Trigger trigger : element.getTriggers()) {
      if (trigger instanceof Scheduled || trigger instanceof Periodic) {
        Future<?> future = scheduledRules.get(trigger);
        if (future != null) {
          // make sure task will be cancelled from following runs, rule is gone!
          future.cancel(false);
        }
      }
      if (trigger instanceof ReadyMarkerTrigger) {
        ReadyMarker marker = ((ReadyMarkerTrigger) trigger).getMarker();
        if (readyMarkerRules.containsKey(marker)) {
          List<Rule> rules = readyMarkerRules.get(marker);
          if (rules.remove(element)) {
            if (rules.isEmpty()) {
              readyMarkerRules.remove(marker);
            }
          }
        }
      }
    }
  }

  private long delay(Periodic period) {
    Clock clock = Clock.fixed(Clock.systemUTC().instant(), ZoneOffset.UTC);
    PastPeriodCalculator past = new PastPeriodCalculator(clock, period.getPeriod());
    FuturePeriodCalculator future = new FuturePeriodCalculator(clock, period.getPeriod());

    Duration duration = Duration.between(past.calculate(), future.calculate());
    return duration.toMillis();
  }

  private long calculateDelay(Scheduled schedule) {
    Period period = Period.SECOND;
    switch (schedule.getTimeUnit()) {
      case NANOSECONDS:
      case MICROSECONDS:
      case MILLISECONDS:
        throw new IllegalArgumentException("Scheduling of high frequency rules is not supported.");
      case MINUTES:
        period = Period.MINUTE;
        break;
      case HOURS:
        period = Period.HOUR;
        break;
      case DAYS:
        period = Period.DAY;
        break;
    }

    Clock clock = Clock.systemUTC();
    long now = clock.millis();
    FuturePeriodCalculator calculator = new FuturePeriodCalculator(clock, period);
    long millisecondDelay = calculator.calculate().toInstant().toEpochMilli() - now;
    schedule.getTimeUnit();
    return millisecondDelay;
  }

  private long calculateDelay(Periodic periodic) {
    Clock clock = Clock.systemUTC();
    long now = clock.millis();
    FuturePeriodCalculator calculator = new FuturePeriodCalculator(clock, periodic.getPeriod());
    return calculator.calculate().toInstant().toEpochMilli() - now;
  }

  @Override
  public void onReadyMarkerAdded(ReadyMarker readyMarker) {
    if (readyMarkerRules.containsKey(readyMarker)) {
      fire(readyMarkerRules.get(readyMarker), (trigger) -> new ReadyMarkerAddedRuleContext(itemRegistry, actionsRegistry, trigger, readyMarker), (trigger -> {
        return trigger instanceof ReadyMarkerAddedTrigger && ((ReadyMarkerTrigger) trigger).getMarker().equals(readyMarker);
      }));
    }
  }

  @Override
  public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    if (readyMarkerRules.containsKey(readyMarker)) {
      fire(readyMarkerRules.get(readyMarker), (trigger) -> new ReadyMarkerRemovedRuleContext(itemRegistry, actionsRegistry, trigger, readyMarker), (trigger -> {
        return trigger instanceof ReadyMarkerRemovedTrigger && ((ReadyMarkerTrigger) trigger).getMarker().equals(readyMarker);
      }));
    }
  }

  static class RuleRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RuleRunnable.class);
    private final Rule rule;
    private final RuleContext context;

    RuleRunnable(Rule rule, RuleContext context) {
      this.rule = rule;
      this.context = context;
    }

    @Override
    public void run() {
      try {
        rule.handle(context);
      } catch (SkipExecutionException e) {
        logger.info("Rule {} execution skipped.", rule);
      } catch (Throwable e) {
        logger.error("Rule {} execution failed.", rule, e);
      }
    }
  }

  static class ScheduledRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RuleRunnable.class);

    private final ItemRegistry itemRegistry;
    private final ThingsActionsRegistry actionsRegistry;
    private final Rule rule;
    private final Trigger trigger;
    private final long registration;
    private long currentRun;
    private long firstRun;
    private Long previousRun;

    public ScheduledRunnable(ItemRegistry itemRegistry, ThingsActionsRegistry actionsRegistry, Rule rule, Trigger trigger) {
      this.itemRegistry = itemRegistry;
      this.actionsRegistry = actionsRegistry;
      this.rule = rule;
      this.trigger = trigger;
      this.registration = System.currentTimeMillis();
    }

    @Override
    public void run() {
      currentRun = System.currentTimeMillis();
      if (firstRun == 0L) {
        firstRun = currentRun;
      }
      try {
        ScheduledRuleContext context = new ScheduledRuleContext(itemRegistry, actionsRegistry, trigger, registration, currentRun, firstRun, previousRun);
        rule.handle(context);
      } catch (SkipExecutionException e) {
        logger.info("Rule {} execution skipped.", rule);
      } catch (Throwable e) {
        logger.error("Rule {} execution failed.", rule, e);
      } finally {
        previousRun = System.currentTimeMillis();
      }
    }
  }

}
