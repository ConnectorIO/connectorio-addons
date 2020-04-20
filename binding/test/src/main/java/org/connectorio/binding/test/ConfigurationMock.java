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
package org.connectorio.binding.test;

import static org.mockito.Mockito.when;

import org.eclipse.smarthome.config.core.Configuration;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class ConfigurationMock<T extends org.connectorio.binding.base.config.Configuration> {

  private final Configuration configuration = Mockito.mock(Configuration.class);

  public Configuration get(T mapped) {
    when(configuration.as(mapped.getClass())).thenAnswer((Answer<Object>) invocation -> mapped);
    return configuration;
  }

}
