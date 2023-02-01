/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.logtail.internal;

import org.connectorio.logtail.LogEntry;

public class PaxLogEntry implements LogEntry {

  private final long timestamp;
  private final String level;
  private final String category;
  private final String message;

  public PaxLogEntry(long timestamp, String level, String category, String message) {
    this.timestamp = timestamp;
    this.level = level;
    this.category = category;
    this.message = message;
  }

  @Override
  public long getTimeStamp() {
    return timestamp;
  }

  @Override
  public String getLevel() {
    return level;
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
