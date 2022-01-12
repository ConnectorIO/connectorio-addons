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

import org.connectorio.addons.norule.Condition;
import org.connectorio.addons.norule.ConditionBuilder;
import org.openhab.core.binding.BindingInfoRegistry;

public class HasBindingCondition implements Condition {

  private final BindingInfoRegistry bindingInfoRegistry;
  private final String binding;

  public HasBindingCondition(BindingInfoRegistry bindingInfoRegistry, String binding) {
    this.bindingInfoRegistry = bindingInfoRegistry;
    this.binding = binding;
  }

  @Override
  public boolean evaluate() {
    return bindingInfoRegistry.getBindingInfo(binding) != null;
  }

}
