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
package org.connectorio.addons.persistence.migrator.internal.operation;

import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseOperation implements Operation {

  private Logger logger;

  // xstream tend to manage all fields, despite of no need for that
  public Logger getLogger() {
    if (logger == null) {
      logger = LoggerFactory.getLogger(getClass());
    }
    return logger;
  }
}
