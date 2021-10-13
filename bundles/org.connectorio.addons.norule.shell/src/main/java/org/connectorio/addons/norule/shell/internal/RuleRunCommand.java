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
import java.util.Set;
import org.connectorio.addons.norule.ItemContext;
import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.RuleContext;
import org.connectorio.addons.norule.RuleRegistry;
import org.connectorio.addons.norule.RuleUID;
import org.connectorio.addons.norule.Trigger;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.ItemRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Utility command to fetch present rules.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class RuleRunCommand extends AbstractConsoleCommandExtension {

  private final RuleRegistry ruleRegistry;

  @Activate
  public RuleRunCommand(@Reference RuleRegistry ruleRegistry) {
    super("co7io-norule-run", "Trigger a rule");
    this.ruleRegistry = ruleRegistry;
  }

  @Override
  public void execute(String[] args, Console console) {
    RuleUID uid = new RuleUID(args[0].split(RuleUID.SEPARATOR));
    ruleRegistry.run(uid);
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-norule-run  rule-uid");
  }

}
