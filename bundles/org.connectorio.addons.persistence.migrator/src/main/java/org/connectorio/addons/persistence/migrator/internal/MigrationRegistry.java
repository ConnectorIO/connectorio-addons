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
package org.connectorio.addons.persistence.migrator.internal;

import org.connectorio.addons.persistence.migrator.operation.Container;
import org.connectorio.addons.persistence.migrator.MigrationProvider;
import org.openhab.core.common.registry.AbstractRegistry;
import org.openhab.core.common.registry.Registry;
import org.osgi.service.component.annotations.Component;

@Component(service = Registry.class, property = {"migrations=true"})
public class MigrationRegistry extends AbstractRegistry<Container, String, MigrationProvider> {

  public MigrationRegistry() {
    super(MigrationProvider.class);
  }
}
