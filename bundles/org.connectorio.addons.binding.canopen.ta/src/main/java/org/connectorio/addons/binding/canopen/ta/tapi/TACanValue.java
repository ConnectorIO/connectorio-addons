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
package org.connectorio.addons.binding.canopen.ta.tapi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.canopen.api.CoNode;

public abstract class TACanValue<T> {

  protected final short index;
  protected final short subIndex;
  protected final CompletableFuture<T> future;
  protected T value;

  public TACanValue(CoNode node, int index, int subIndex, CANOpenDataType type) {
    this(node, (short) index, (short) subIndex, type);
  }

  public TACanValue(CoNode node, short index, short subIndex, CANOpenDataType type) {
    this.index = index;
    this.subIndex = subIndex;
    this.future = initialize(node, type);
  }

  protected CompletableFuture<T> initialize(CoNode node, CANOpenDataType type) {
    // todo maybe do some type matching with type parameter .. ?
    return initialize(node.read(index, subIndex));
  }

  protected abstract CompletableFuture<T> initialize(CompletableFuture<byte[]> read);

  public CompletableFuture<T> toFuture() {
    return future;
  }

  public T read() {
    // set only if SDO retrieval succeeded
    return value;
  }

  public Optional<T> get() {
    return Optional.ofNullable(value);
  }

  public String toString() {
    String output = getClass().getSimpleName() + "(";
    if (value != null) {
      output += value + ") [";
    } else {
      output += "unresolved) [";
    }

    return output + (Integer.toHexString(index) + ", " + Integer.toHexString(subIndex) + "]");
  }

}
