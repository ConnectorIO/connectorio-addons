/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.temporal.item.internal;

import java.util.Arrays;
import java.util.List;
import org.connectorio.addons.temporal.calendar.CalendarType;
import org.connectorio.addons.temporal.item.TemporalItemFactory;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class CalendarItem extends GenericItem implements Item {

  public CalendarItem(String itemName) {
    super(TemporalItemFactory.CALENDAR, itemName);
  }

  @Override
  public List<Class<? extends State>> getAcceptedDataTypes() {
    return Arrays.asList(
      CalendarType.class,
      StringType.class
    );
  }

  @Override
  public List<Class<? extends Command>> getAcceptedCommandTypes() {
    return Arrays.asList(
      CalendarType.class,
      StringType.class
    );
  }

}
