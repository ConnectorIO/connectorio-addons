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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.connectorio.addons.persistence.migrator.CompositeExecutionStatus;
import org.connectorio.addons.persistence.migrator.operation.Container;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.connectorio.addons.persistence.migrator.operation.Statuses;

public class MigrationExecutionStatus extends DefaultExecutionStatus<Container> implements CompositeExecutionStatus<Container> {

  private final Map<Operation, Status> statuses = new LinkedHashMap<>();
  private boolean failed;
  private boolean success;

  public MigrationExecutionStatus(Status status, Container migration) {
    super(status, migration);
  }

  public void failed() {
    failed = true;
  }

  public void success() {
    success = true;
  }

  @Override
  public Status getStatus() {
    return failed ? Statuses.FAILURE : (success ? Statuses.SUCCESS : super.getStatus());
  }

  public void add(Operation operation, Status status) {
    statuses.put(operation, status);
  }

  @Override
  public Optional<Status> getStatus(Object element) {
    return Optional.ofNullable(statuses.get(element));
  }

}
