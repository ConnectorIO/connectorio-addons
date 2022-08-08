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
package org.connectorio.addons.norule.shell.internal;

import java.util.Arrays;
import java.util.List;
import org.connectorio.addons.norule.RuleExecutor;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

/**
 * Utility command to display stats.
 */
public class RuleStatsCommand extends AbstractConsoleCommandExtension {

  private final RuleExecutor ruleExecutor;

  @Activate
  public RuleStatsCommand(@Reference RuleExecutor ruleExecutor) {
    super("co7io-norule-stats", "Rule statistics");
    this.ruleExecutor = ruleExecutor;
  }

  @Override
  public void execute(String[] args, Console console) {
    console.println("Rule statistics:");
    int activateExecutions = ruleExecutor.getActivateCount();
    console.println("  Active: " + activateExecutions);
    console.println("   Total: " + ruleExecutor.getExecutionsCounter());
    console.println("Failures: " + ruleExecutor.getFailuresCounter());
    console.println("Active:");
    List<String> executions = ruleExecutor.getActivateExecutions();
    for (String active : executions) {
      console.println("  " + active);
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-norule-stats");
  }

}
