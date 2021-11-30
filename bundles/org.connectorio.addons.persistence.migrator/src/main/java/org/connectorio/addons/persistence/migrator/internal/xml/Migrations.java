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
package org.connectorio.addons.persistence.migrator.internal.xml;

import java.util.List;
import java.util.Objects;
import org.connectorio.addons.persistence.migrator.operation.Container;
import org.connectorio.addons.persistence.migrator.operation.Operation;

public class Migrations implements Container {

  private List<Operation> steps;
  private String service;
  private String uid;

  @Override
  public List<Operation> getSteps() {
    return steps;
  }

  public void setSteps(List<Operation> steps) {
    this.steps = steps;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  @Override
  public String getUID() {
    return uid;
  }

  public void setUID(String uid) {
    this.uid = uid;
  }

  public String toString() {
    return "MigrationContainer [uid=" + uid + ", service=" + service + ", size=" + steps.size() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Migrations)) {
      return false;
    }
    Migrations that = (Migrations) o;
    return Objects.equals(getSteps(), that.getSteps()) && Objects.equals(getService(), that.getService())
      && Objects.equals(uid, that.uid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSteps(), getService(), uid);
  }

}
