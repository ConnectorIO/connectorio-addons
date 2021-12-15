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
package org.connectorio.addons.persistence.migrator.shell;

import java.util.Arrays;
import java.util.List;
import org.connectorio.addons.persistence.migrator.CompositeExecutionStatus;
import org.connectorio.addons.persistence.migrator.operation.Container;
import org.connectorio.addons.persistence.migrator.MigrationManager;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which allows working with migrations.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class MigratorShell extends AbstractConsoleCommandExtension {

  private final MigrationManager migrationManager;

  @Activate
  public MigratorShell(@Reference MigrationManager migrationManager) {
    super("co7io-persistence-migrator", "Migrator shell");
    this.migrationManager = migrationManager;
  }

  @Override
  public void execute(String[] args, Console console) {
    if (args.length != 1) {
      console.println("Specify sub-command to be executed");
      return;
    }

    if ("status".equals(args[0])) {
      console.println("     Status   |    Migration");
      console.println("--------------+-----------------");
      for (CompositeExecutionStatus<Container> status : migrationManager.getExecutionStatus()) {
        Container container = status.getElement();
        console.println(String.format("   %10s | %s", status.getStatus().getCode(), container));
        List<Operation> steps = container.getSteps();
        for (int index = 0, stepsSize = steps.size(); index < stepsSize; index++) {
          Operation step = steps.get(index);
          String subStatus = status.getStatus(step)
            .map(Status::getCode)
            .orElse("???");
          console.println(String.format("%2d) %9s | %s", index + 1, subStatus, step));
        }
      }
      console.println("--------------+-----------------");
      console.println("Success: " + migrationManager.isAllMigrationsSucceeded());
    }
    if ("success".equals(args[0])) {
      console.println("" + migrationManager.isAllMigrationsSucceeded());
    }
    if ("run".equals(args[0])) {
      migrationManager.execute();
      console.println("" + migrationManager.isAllMigrationsSucceeded());
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-persistence-migrator [sub-command]\n" +
      "Supported sub-commands: \n" +
      "   status: prints table of discovered migrations and their execution status \n" +
      "  success: prints true if all migrations are considered as succeeded (no failures nro waiting elements reported) \n" +
      "      run: force execution of migrations \n"
    );
  }

}
