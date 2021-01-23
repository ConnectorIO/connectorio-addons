/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.automation.calculation.internal.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import org.openhab.core.automation.type.ModuleType;
import org.openhab.core.automation.type.ModuleTypeProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
@SuppressWarnings("unchecked")
public class PersistenceServiceCalculationModuleTypeProvider implements ModuleTypeProvider {

  private final PersistenceServiceCalculationActionType module;

  @Activate
  public PersistenceServiceCalculationModuleTypeProvider(@Reference PersistenceServiceRegistry persistenceServiceRegistry) {
    this.module = new PersistenceServiceCalculationActionType(persistenceServiceRegistry);
  }

  @Override
  public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
    return (T) this.module;
  }

  @Override
  public <T extends ModuleType> Collection<T> getModuleTypes(Locale locale) {
    return (Collection<T>) Collections.singleton(module);
  }

  @Override
  public Collection<ModuleType> getAll() {
    return Collections.singleton(module);
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
    // does nothing because this provider does not change
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
    // does nothing because this provider does not change
  }


}
