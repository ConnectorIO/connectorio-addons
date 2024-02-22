/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.persistence.memory.internal;

import java.util.Date;
import org.openhab.core.persistence.PersistenceItemInfo;

public class MemoryBucketInfo implements PersistenceItemInfo {

  private final String name;
  private final Integer count;
  private final Date earliest;
  private final Date oldest;

  public MemoryBucketInfo(String name, Integer count, Date earliest, Date oldest) {
    this.name = name;
    this.count = count;
    this.earliest = earliest;
    this.oldest = oldest;
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public Integer getCount() {
    return count;
  }

  @Override
  public Date getEarliest() {
    return earliest;
  }

  @Override
  public Date getLatest() {
    return oldest;
  }
}
