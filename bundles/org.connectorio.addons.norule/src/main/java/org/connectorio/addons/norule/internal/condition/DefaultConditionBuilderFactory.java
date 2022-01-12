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
package org.connectorio.addons.norule.internal.condition;

import org.connectorio.addons.norule.ConditionBuilder;
import org.connectorio.addons.norule.ConditionBuilderFactory;
import org.openhab.core.binding.BindingInfoRegistry;
import org.openhab.core.service.ReadyService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class DefaultConditionBuilderFactory implements ConditionBuilderFactory {

  private final ReadyService readyService;
  private final BindingInfoRegistry bindingInfoRegistry;

  @Activate
  public DefaultConditionBuilderFactory(@Reference ReadyService readyService, @Reference BindingInfoRegistry bindingInfoRegistry) {
    this.readyService = readyService;
    this.bindingInfoRegistry = bindingInfoRegistry;
  }

  @Override
  public ConditionBuilder createBuilder() {
    return new DefaultConditionBuilder(readyService, bindingInfoRegistry);
  }

}
