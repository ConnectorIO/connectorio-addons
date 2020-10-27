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
package org.connectorio.binding.plc4x.canopen.ta.internal.config;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;

public enum DigitalUnit {

  /* 43 digital */ OPEN_CLOSED (43, OpenClosedType.OPEN, OpenClosedType.CLOSED),
  /* 44 digital */ ON_OFF (44, OnOffType.OFF, OnOffType.ON),
  /* 47 digital */ UP_DOWN (47, OnOffType.OFF /* STOP ? */, OnOffType.ON, OnOffType.OFF);

  private final static Map<Integer, DigitalUnit> UNIT_MAP = Arrays.stream(values()).collect(Collectors.toMap(
    DigitalUnit::getIndex,
    value -> value,
    (left, right) -> left
  ));

  private final int index;
  private final State[] states;

  DigitalUnit(int index, State... states) {
    this.index = index;
    this.states = states;
  };

  public int getIndex() {
    return index;
  }

  public State parse(int value) {
    return states[value];
  }

  public static DigitalUnit valueOf(int index) {
    return UNIT_MAP.get(index);
  }

}
