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
import org.connectorio.addons.norule.RuleManager;
import org.connectorio.addons.norule.RuleRegistry;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ConsoleCommandExtension.class)
public class NoRuleCommandExtension extends AbstractConsoleCommandExtension {

  public static final String NORULE = "norule";

  private static final String SUBCMD_RUN = "run";
  private static final String SUBCMD_LIST = "list";
  private final RuleManager ruleManager;
  private final RuleRegistry ruleRegistry;

  @Activate
  public NoRuleCommandExtension(@Reference RuleManager ruleManager, @Reference RuleRegistry ruleRegistry) {
    super(NORULE, "Browse and fire system managed rules.");
    this.ruleManager = ruleManager;
    this.ruleRegistry = ruleRegistry;
  }

  @Override
  public void execute(String[] args, Console console) {
    if (args.length > 0) {
      switch (args[0]) {
        case SUBCMD_RUN:
          new RuleRunCommand(ruleManager).execute(trimArgs(args), console);
          return;
        case SUBCMD_LIST:
          new RuleListCommand(ruleRegistry).execute(trimArgs(args), console);
          return;
      }
      return;
    }
    printUsage(console);
  }

  private String[] trimArgs(String[] args) {
    if (args.length < 1) {
      return new String[0];
    }
    String[] trimmed = new String[args.length - 1];
    System.arraycopy(args, 1, trimmed, 0, args.length - 1);
    return trimmed;
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList(
      buildCommandUsage(SUBCMD_RUN + " <rule-uid>", "Fire a rule using system trigger. Be aware that some rules might not react properly to manual execution."),
      buildCommandUsage(SUBCMD_LIST, "List all rules known to the system.")
    );
  }

}
