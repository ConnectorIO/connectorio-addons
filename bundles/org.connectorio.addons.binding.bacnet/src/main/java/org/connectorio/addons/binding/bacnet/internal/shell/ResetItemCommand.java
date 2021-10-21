/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.shell;

import java.util.Arrays;
import java.util.List;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ConsoleCommandExtension.class)
public class ResetItemCommand extends AbstractConsoleCommandExtension {

  private final ItemRegistry itemRegistry;

  @Activate
  public ResetItemCommand(@Reference ItemRegistry itemRegistry) {
    super("bacnet", "BACnet related operations.");
    this.itemRegistry = itemRegistry;
  }

  @Override
  public void execute(String[] args, Console console) {
    if (args.length < 2) {
      console.println("To much or to little arguments passed.");
      getUsages().forEach(console::println);
      return;
    }

    if (!"reset".equals(args[0])) {
      console.println("Wrong command");
      return;
    }

    Item item = itemRegistry.get(args[1]);
    if (item == null) {
      console.println("Item " + args[1] + " not found");
      return;
    }

    if (item instanceof GenericItem) {
      ((GenericItem) item).setState(UnDefType.NULL);
      console.println("Item " + args[1] + " set to NULL.");
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("bacnet reset ITEM - reset state of item to NULL");
  }

}

