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

import java.util.Objects;
import org.connectorio.addons.persistence.migrator.internal.xml.ItemReference;

public abstract class ItemPairOperation extends ItemOperation {

  private ItemReference target;

  public ItemPairOperation(String sourceService, String sourceItem, String targetItem, String targetService) {
    super(sourceItem, sourceService);
    this.target = new ItemReference(targetItem, targetService);
  }

  public ItemReference getTarget() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ItemPairOperation)) {
      return false;
    }
    ItemPairOperation that = (ItemPairOperation) o;
    return Objects.equals(getTarget(), that.getTarget());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTarget());
  }
}
