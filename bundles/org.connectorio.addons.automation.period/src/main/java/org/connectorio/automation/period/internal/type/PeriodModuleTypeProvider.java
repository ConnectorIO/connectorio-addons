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
package org.connectorio.automation.period.internal.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import org.openhab.core.automation.type.ModuleType;
import org.openhab.core.automation.type.ModuleTypeProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Component;

@Component
@SuppressWarnings("unchecked")
public class PeriodModuleTypeProvider implements ModuleTypeProvider {

  @Override
  public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
    return (T) PeriodTriggerType.INSTANCE;
  }

  @Override
  public <T extends ModuleType> Collection<T> getModuleTypes(Locale locale) {
    return (Collection<T>) getAll();
  }

  @Override
  public Collection<ModuleType> getAll() {
    return Collections.singletonList(PeriodTriggerType.INSTANCE);
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
  }

}
