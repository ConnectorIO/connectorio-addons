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

import java.util.Comparator;
import java.util.Date;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryBucket {

  private final Logger logger = LoggerFactory.getLogger(MemoryBucket.class);

  private final NavigableSet<MemoryEntry> entries = new TreeSet<>(
    Comparator.comparing(MemoryEntry::getTimestamp)
  );

  private final Lock lock = new ReentrantLock();
  private int limit;

  public MemoryBucket(int limit) {
    this.limit = limit;
  }

  public void append(MemoryEntry entry) {
    apply(myself -> {
      logger.trace("Inserted entry {}. Stored entries {}, limit {}", entry, entries.size(), limit);
      myself.entries.add(entry);
    });
  }

  public void truncate(int limit) {
    this.limit = limit;

    apply(myself -> {
      while (myself.entries.size() > limit) {
        MemoryEntry entry = myself.entries.pollFirst();
        logger.trace("Removed bucket entry {} as it exceeds limit {}", entry, limit);
      }
    });
  }

  public Stream<MemoryEntry> entries() {
    return entries.stream();
  }

  public void remove(MemoryEntry entry) {
    apply(myself -> myself.entries.remove(entry));
  }

  public Integer getSize() {
    return entries.size();
  }

  public Date getEarliest() {
    return entries.isEmpty() ? null : Date.from(entries.first().getTimestamp().toInstant());
  }

  public Date getOldest() {
    return entries.isEmpty() ? null : Date.from(entries.last().getTimestamp().toInstant());
  }

  public <X> X process(Function<MemoryBucket, X> function) {
    lock.lock();
    try {
      return function.apply(this);
    } finally {
      lock.unlock();
    }
  }

  private void apply(Consumer<MemoryBucket> consumer) {
    lock.lock();
    try {
      consumer.accept(this);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MemoryBucket)) {
      return false;
    }
    MemoryBucket bucket = (MemoryBucket) o;
    return limit == bucket.limit && Objects.equals(entries, bucket.entries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entries, limit);
  }

}
