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
package org.connectorio.automation.period.internal.handler;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.connectorio.automation.period.PeriodTriggerConstants;
import org.connectorio.automation.period.internal.config.PeriodTriggerConfig;
import org.connectorio.chrono.shared.FuturePeriodCalculator;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseTriggerModuleHandler;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodTriggerHandler extends BaseTriggerModuleHandler implements Callable<ZonedDateTime> {

  private final Logger logger = LoggerFactory.getLogger(PeriodTriggerHandler.class);

  private final Scheduler scheduler;
  private final FuturePeriodCalculator calculator;
  private ZonedDateTime dateTime;
  private ZonedDateTime previousInstant;

  private ScheduledCompletableFuture<?> schedule;

  public PeriodTriggerHandler(Trigger module, Clock clock, Scheduler scheduler) {
    super(module);
    this.scheduler = scheduler;

    PeriodTriggerConfig config = module.getConfiguration().as(PeriodTriggerConfig.class);
    if (config.period == null) {
      throw new IllegalArgumentException("Period must be set");
    }

    calculator = new FuturePeriodCalculator(clock, config.period);
  }

  @Override
  public synchronized void setCallback(ModuleHandlerCallback callback) {
    super.setCallback(callback);
    if (calculator.getPeriod() != null) {
      scheduleJob();
    }
  }

  private void scheduleJob() {
    dateTime = calculator.calculate();
    schedule = scheduler.at(this, dateTime.toInstant());
    logger.debug("Scheduled next execution of trigger '{}' to {} ({}).", module.getId(), dateTime, calculator);
  }

  @Override
  public synchronized void dispose() {
    super.dispose();

    if (schedule != null) {
      schedule.cancel(true);
      logger.debug("Terminating trigger '{}'.", module.getId());
    }
  }

  @Override
  public ZonedDateTime call() {
    logger.trace("Trigger '{}' activated.", module.getId());
    Map<String, Object> context = new HashMap<>();
    context.put(PeriodTriggerConstants.TRIGGER_TIME, dateTime);
    context.put(PeriodTriggerConstants.PREVIOUS_TRIGGER_TIME, previousInstant);
    previousInstant = dateTime;
    ((TriggerHandlerCallback) callback).triggered(module, context);
    scheduleJob();
    return dateTime;
  }

}
