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
package org.connectorio.addons.norule.internal.context;

import java.util.Optional;
import org.connectorio.addons.norule.ItemContext;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyRuleContext implements ItemContext {

  private final Logger logger = LoggerFactory.getLogger(EmptyRuleContext.class);
  private final String itemName;

  public EmptyRuleContext(String itemName) {
    this.itemName = itemName;
  }

  @Override
  public Optional<State> state() {
    return Optional.empty();
  }

  @Override
  public <X extends State> Optional<X> state(Class<X> type) {
    logger.info("Could not retrieve state of item {} as it does not exist", itemName);
    return Optional.empty();
  }

  @Override
  public void state(State state) {
    logger.info("Could not update state of item {} as it does not exist", itemName);
  }

}
