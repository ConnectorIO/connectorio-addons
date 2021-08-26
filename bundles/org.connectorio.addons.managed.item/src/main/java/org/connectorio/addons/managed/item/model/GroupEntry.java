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
package org.connectorio.addons.managed.item.model;

import java.util.List;
import java.util.Objects;

public class GroupEntry extends ItemEntry {

  private String baseItemType;
  private String function;
  private List<String> parameters;
  private List<String> members;

  public String getBaseItemType() {
    return baseItemType;
  }

  public void setBaseItemType(String baseItemType) {
    this.baseItemType = baseItemType;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GroupEntry)) {
      return false;
    }
    GroupEntry that = (GroupEntry) o;
    return Objects.equals(baseItemType, that.baseItemType) &&
      Objects.equals(function, that.function) &&
      Objects.equals(parameters, that.parameters) &&
      Objects.equals(members, that.members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseItemType, function, parameters, members);
  }
}
