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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.connectorio.addons.norule.Condition;
import org.connectorio.addons.norule.Periodic;
import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.RuleContext;
import org.connectorio.addons.norule.RuleManager;
import org.connectorio.addons.norule.RuleRegistry;
import org.connectorio.addons.norule.RuleUID;
import org.connectorio.addons.norule.Scheduled;
import org.connectorio.addons.norule.SkipExecutionException;
import org.connectorio.addons.norule.StateDispatcher;
import org.connectorio.addons.norule.ThingActionsRegistry;
import org.connectorio.addons.norule.Trigger;
import org.connectorio.addons.norule.internal.context.EmptyTriggerRuleContext;
import org.connectorio.addons.norule.internal.context.ItemStateChangeRuleContext;
import org.connectorio.addons.norule.internal.context.ItemStateUpdateRuleContext;
import org.connectorio.addons.norule.internal.context.MemberStateChangeRuleContext;
import org.connectorio.addons.norule.internal.context.MemberStateUpdateRuleContext;
import org.connectorio.addons.norule.internal.context.ReadyMarkerAddedRuleContext;
import org.connectorio.addons.norule.internal.context.ReadyMarkerRemovedRuleContext;
import org.connectorio.addons.norule.internal.context.ScheduledRuleContext;
import org.connectorio.addons.norule.internal.context.StartLevelRuleContext;
import org.connectorio.addons.norule.internal.context.ThingStatusChangeRuleContext;
import org.connectorio.addons.norule.internal.context.ThingStatusRuleContext;
import org.connectorio.addons.norule.RuleExecutor;
import org.connectorio.addons.norule.internal.dispatch.FallbackStateDispatcher;
import org.connectorio.addons.norule.internal.dispatch.EventPublisherStateDispatcher;
import org.connectorio.addons.norule.internal.executor.DefaultRuleExecutor;
import org.connectorio.addons.norule.internal.trigger.EmptyTrigger;
import org.connectorio.addons.norule.internal.trigger.MemberStateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.MemberStateUpdateTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerAddedTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerRemovedTrigger;
import org.connectorio.addons.norule.internal.trigger.ReadyMarkerTrigger;
import org.connectorio.addons.norule.internal.trigger.StartLevelTrigger;
import org.connectorio.addons.norule.internal.trigger.StateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.StateUpdateTrigger;
import org.connectorio.addons.norule.internal.trigger.ThingStatusChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.ThingStatusTrigger;
import org.connectorio.addons.norule.internal.trigger.ThingReferenceTrigger;
import org.connectorio.chrono.Period;
import org.connectorio.chrono.shared.FuturePeriodCalculator;
import org.connectorio.chrono.shared.PastPeriodCalculator;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.events.system.StartlevelEvent;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.GroupItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.openhab.core.thing.events.ThingStatusInfoEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {RuleManager.class, ReadyTracker.class, EventSubscriber.class})
public class NoRuleManager implements RuleManager, ReadyTracker, EventSubscriber, RegistryChangeListener<Rule> {

  // marker needed to activate norule engine
  private static final ReadyMarker PERSISTENCE_RESTORE = new ReadyMarker("persistence", "restore");

  private final Logger logger = LoggerFactory.getLogger(NoRuleRegistry.class);
  private final Set<Rule> activeRules = new CopyOnWriteArraySet<>();
  private final Map<Trigger, Future<?>> scheduledRules = new ConcurrentHashMap<>();
  private final Map<ReadyMarker, List<Rule>> readyMarkerRules = new ConcurrentHashMap<>();
  private final RuleRegistry ruleRegistry;
  private final RuleExecutor ruleExecutor;
  private final ThingRegistry thingRegistry;
  private final ItemRegistry itemRegistry;
  private final ReadyService readyService;
  private final ThingActionsRegistry actionsRegistry;
  private final StateDispatcher stateDispatcher;
  private final AtomicInteger startLevel = new AtomicInteger();

  public NoRuleManager(RuleRegistry ruleRegistry,
      ThingRegistry thingRegistry, ItemRegistry itemRegistry,
      ReadyService readyService, ThingActionsRegistry actionsRegistry) {
    this(ruleRegistry, new DefaultRuleExecutor(), thingRegistry, itemRegistry, readyService, actionsRegistry,
      new FallbackStateDispatcher()
    );
  }

  @Activate
  public NoRuleManager(@Reference RuleRegistry ruleRegistry, @Reference RuleExecutor ruleExecutor,
    @Reference ThingRegistry thingRegistry, @Reference ItemRegistry itemRegistry,
    @Reference ReadyService readyService, @Reference ThingActionsRegistry actionsRegistry,
    @Reference EventPublisher eventPublisher) {
    this(ruleRegistry, ruleExecutor, thingRegistry, itemRegistry, readyService, actionsRegistry,
      new EventPublisherStateDispatcher(eventPublisher)
    );
  }

  protected NoRuleManager(RuleRegistry ruleRegistry, RuleExecutor ruleExecutor,
      ThingRegistry thingRegistry, ItemRegistry itemRegistry,
      ReadyService readyService, ThingActionsRegistry actionsRegistry,
      StateDispatcher stateDispatcher) {
    this.ruleRegistry = ruleRegistry;
    this.ruleExecutor = ruleExecutor;
    this.thingRegistry = thingRegistry;
    this.itemRegistry = itemRegistry;
    this.readyService = readyService;
    this.actionsRegistry = actionsRegistry;
    this.stateDispatcher = stateDispatcher;
    readyService.registerTracker(this, new ReadyMarkerFilter());
    ruleRegistry.addRegistryChangeListener(this);

    for (Rule rule : ruleRegistry.getAll()) {
      added(rule);
    }
  }

  @Deactivate
  void shutdown() {
    readyService.unmarkReady(READY_MARKER);
    readyService.unregisterTracker(this);
    ruleRegistry.removeRegistryChangeListener(this);
    ruleExecutor.shutdown();
  }

  @Override
  public void run(RuleUID ruleUID) {
    Rule rule = ruleRegistry.get(ruleUID);
    if (rule != null) {
      Scheduled schedule = null;
      for (Trigger trigger : rule.getTriggers()) {
        if (trigger instanceof Scheduled) {
          schedule = (Scheduled) trigger;
          break;
        }
      }
      if (schedule != null) {
        ruleExecutor.execute(new ScheduledRunnable(itemRegistry, actionsRegistry, rule, stateDispatcher, schedule));
      } else {
        ruleExecutor.execute(new RuleRunnable(rule, new EmptyTriggerRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, new EmptyTrigger())));
      }
    }
  }

  @Override
  public Set<String> getSubscribedEventTypes() {
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      GroupItemStateChangedEvent.TYPE,
      ItemStateEvent.TYPE,
      ItemStateChangedEvent.TYPE,
      StartlevelEvent.TYPE,
      ThingStatusInfoEvent.TYPE,
      ThingStatusInfoChangedEvent.TYPE
    )));
  }

  @Override
  public EventFilter getEventFilter() {
    return null;
  }

  @Override
  public void receive(Event event) {
    if (event instanceof GroupItemStateChangedEvent) {
// err.. its not fired by system itself, at least seems to be a case
//      GroupItemStateChangedEvent stateChangeEvent = (GroupItemStateChangedEvent) event;
//      fire((rule, trigger) -> new ItemStateChangeContext(rule, itemRegistry, actionsRegistry, trigger, stateChangeEvent.getItemName(), stateChangeEvent.getItemState(), currentState), (trigger) -> {
//        return trigger instanceof GroupStateChangeTrigger && stateChangeEvent.getItemName().equals(((GroupStateChangeTrigger) trigger).getGroupName());
//      });
    } else if (event instanceof ItemStateChangedEvent) {
      ItemStateChangedEvent stateChangeEvent = (ItemStateChangedEvent) event;
      fire((rule, trigger) -> new ItemStateChangeRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, stateChangeEvent.getItemName(), stateChangeEvent.getOldItemState(), stateChangeEvent.getItemState()), (trigger) -> {
        return trigger instanceof StateChangeTrigger && ((StateChangeTrigger) trigger).matches(stateChangeEvent.getItemName());
      });
      fire((rule, trigger) -> new MemberStateChangeRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, ((MemberStateChangeTrigger) trigger).getGroupName(), stateChangeEvent.getItemName(), stateChangeEvent.getOldItemState(), stateChangeEvent.getItemState()), (trigger) -> {
        return trigger instanceof MemberStateChangeTrigger && isMemberOf(((MemberStateChangeTrigger) trigger).getGroupName(), stateChangeEvent.getItemName());
      });
    } else if (event instanceof ItemStateEvent) {
      ItemStateEvent stateEvent = (ItemStateEvent) event;
      fire((rule, trigger) -> new ItemStateUpdateRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, stateEvent.getItemName(), stateEvent.getItemState()), (trigger -> {
        return trigger instanceof StateUpdateTrigger && ((StateUpdateTrigger) trigger).matches(stateEvent.getItemName());
      }));
      fire((rule, trigger) -> new MemberStateUpdateRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, ((MemberStateUpdateTrigger) trigger).getGroupName(), stateEvent.getItemName(), stateEvent.getItemState()), (trigger) -> {
        return trigger instanceof MemberStateUpdateTrigger && isMemberOf(((MemberStateUpdateTrigger) trigger).getGroupName(), stateEvent.getItemName());
      });
    } else if (event instanceof StartlevelEvent) {
      StartlevelEvent startlevelEvent = (StartlevelEvent) event;
      fire((rule, trigger) -> new StartLevelRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, startLevel.get(), startlevelEvent.getStartlevel()), (trigger -> {
        return trigger instanceof StartLevelTrigger && startlevelEvent.getStartlevel() <= ((StartLevelTrigger) trigger).getStartLevel();
      }));
      startLevel.set(startlevelEvent.getStartlevel());
    } else if (event instanceof ThingStatusInfoChangedEvent) {
      ThingStatusInfoChangedEvent status = (ThingStatusInfoChangedEvent) event;
      ThingUID thing = status.getThingUID();
      fire((rule, trigger) -> new ThingStatusChangeRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, thingRegistry.get(thing), status.getStatusInfo(), status.getOldStatusInfo()), (trigger -> {
        return trigger instanceof ThingStatusChangeTrigger &&  ((ThingReferenceTrigger) trigger).getPredicate().test(thing);
      }));
    } else if (event instanceof ThingStatusInfoEvent) {
      ThingStatusInfoEvent status = (ThingStatusInfoEvent) event;
      ThingUID thing = status.getThingUID();
      fire((rule, trigger) -> new ThingStatusRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, thingRegistry.get(thing), status.getStatusInfo()), (trigger -> {
        return trigger instanceof ThingStatusTrigger &&  ((ThingReferenceTrigger) trigger).getPredicate().test(thing);
      }));
    } else {
      logger.debug("Unsupported event received {}", event);
    }
  }

  private boolean isMemberOf(String groupName, String itemName) {
    Item item = itemRegistry.get(groupName);
    if (!(item instanceof GroupItem)) {
      return false;
    }

    GroupItem group = (GroupItem) item;
    for (Item member : group.getAllMembers()) {
      if (member.getName().equals(itemName)) {
        return true;
      }
    }
    return false;
  }

  private final void fire(BiFunction<Rule, Trigger, RuleContext> contextFactory, Predicate<Trigger> predicate) {
    fire(ruleRegistry.getAll(), contextFactory, predicate);
  }

  private final void fire(Collection<Rule> rules, BiFunction<Rule, Trigger, RuleContext> contextFactory, Predicate<Trigger> predicate) {
    for (Rule rule : rules) {
      for (Trigger trigger : rule.getTriggers()) {
        if (predicate.test(trigger)) {
          Set<Condition> conditions = rule.getConditions();
          Condition block;
          if (conditions != null && (block = isBlocked(conditions)) != null) {
            logger.debug("Not firing rule {} triggered by {}, condition {} is not met.", rule, trigger, block);
            break;
          }
          logger.trace("Rule {} is triggered by {}.", rule, trigger);
          ruleExecutor.submit(new RuleRunnable(rule, contextFactory.apply(rule, trigger)));
        }
      }
    }
  }

  private Condition isBlocked(Set<Condition> conditions) {
    for (Condition condition : conditions) {
      if (!condition.evaluate()) {
        return condition;
      }
    }
    return null;
  }

  @Override
  public void added(Rule element) throws IllegalArgumentException {
    if (activate(element)) { // avoid duplicate registration of rules
      return;
    }

    for (Trigger trigger : element.getTriggers()) {
      if (trigger instanceof Scheduled) {
        Scheduled schedule = (Scheduled) trigger;
        TimeUnit delayUnit = TimeUnit.MILLISECONDS;
        long initialDelay = calculateDelay(schedule);
        logger.debug("First run of scheduled rule {} in {} ms ({} {}). Then every {} {}", element, initialDelay,
          schedule.getTimeUnit().convert(initialDelay, delayUnit), schedule.getTimeUnit(),
          schedule.getDelay(), schedule.getTimeUnit()
        );
        ScheduledFuture<?> future = ruleExecutor.scheduleAtFixedRate(
          new ScheduledRunnable(itemRegistry, actionsRegistry, element, stateDispatcher, schedule),
          initialDelay,
          delayUnit.convert(schedule.getDelay(), schedule.getTimeUnit()),
          delayUnit
        );
        scheduledRules.put(schedule, future);
      }
      if (trigger instanceof Periodic) {
        Periodic periodic = (Periodic) trigger;
        long initialDelay = calculateDelay(periodic);
        long milli = delay(periodic);
        logger.debug("First run of periodic rule {} in {} ms. Then every {} ms", element, initialDelay, milli);
        ScheduledFuture<?> future = ruleExecutor.scheduleAtFixedRate(
          new ScheduledRunnable(itemRegistry, actionsRegistry, element, stateDispatcher, periodic),
          initialDelay,
          milli,
          TimeUnit.MILLISECONDS
        );
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
  public void removed(Rule element) {
    deactivate(element);

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

  @Override
  public void updated(Rule oldElement, Rule element) {
    removed(oldElement);
    added(element);
  }

  private boolean activate(Rule element) {
    return activeRules.contains(element);
  }

  private boolean deactivate(Rule element) {
    return activeRules.remove(element);
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
    if (readyMarker.equals(PERSISTENCE_RESTORE)) {
      readyService.markReady(READY_MARKER);
    }
    if (readyMarkerRules.containsKey(readyMarker)) {
      fire(readyMarkerRules.get(readyMarker), (rule, trigger) -> new ReadyMarkerAddedRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, readyMarker), (trigger -> {
        return trigger instanceof ReadyMarkerAddedTrigger && ((ReadyMarkerTrigger) trigger).getMarker().equals(readyMarker);
      }));
    }
  }

  @Override
  public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    if (readyMarkerRules.containsKey(readyMarker)) {
      fire(readyMarkerRules.get(readyMarker), (rule, trigger) -> new ReadyMarkerRemovedRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, readyMarker), (trigger -> {
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

    public String toString() {
      return rule.getUID().toString();
    }
  }

  static class ScheduledRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RuleRunnable.class);

    private final ItemRegistry itemRegistry;
    private final ThingActionsRegistry actionsRegistry;
    private final Rule rule;
    private final StateDispatcher stateDispatcher;
    private final Trigger trigger;
    private final long registration;
    private long currentRun;
    private long firstRun;
    private Long previousRun;

    public ScheduledRunnable(ItemRegistry itemRegistry, ThingActionsRegistry actionsRegistry, Rule rule, StateDispatcher stateDispatcher, Trigger trigger) {
      this.itemRegistry = itemRegistry;
      this.actionsRegistry = actionsRegistry;
      this.rule = rule;
      this.stateDispatcher = stateDispatcher;
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
        ScheduledRuleContext context = new ScheduledRuleContext(rule, itemRegistry, actionsRegistry, stateDispatcher, trigger, registration, currentRun, firstRun, previousRun);
        rule.handle(context);
      } catch (SkipExecutionException e) {
        logger.info("Rule {} execution skipped.", rule);
      } catch (Throwable e) {
        logger.error("Rule {} execution failed.", rule, e);
      } finally {
        previousRun = System.currentTimeMillis();
      }
    }

    public String toString() {
      return rule.getUID().toString();
    }
  }

}
