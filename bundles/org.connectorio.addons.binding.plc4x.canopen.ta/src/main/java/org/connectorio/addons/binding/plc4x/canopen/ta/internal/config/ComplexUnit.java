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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

/**
 * Compound units whcih require additional parsing. Result of computation is list and not single value.
 */
public enum ComplexUnit implements TAUnit {

  /* 46 analog  */ RAS_TEMPERATURE(46, (Integer raw) -> {
    boolean negative = (raw & 0x8000) != 0;

    double value = 0.1 * (raw & 0x1FF) * (negative ? -1 : 1);
    return Arrays.asList(
      Quantities.getQuantity(value, Units.CELSIUS),
      (raw & 0x600) >> 9
    );
  });

  private final static Map<Integer, ComplexUnit> UNIT_MAP = Arrays.stream(values()).collect(Collectors.toMap(
    ComplexUnit::getIndex,
    value -> value,
    (left, right) -> left
  ));

  private final int index;
  private final Function<Integer, List<Object>> mapper;

  ComplexUnit(int index, Function<Integer, List<Object>> mapper) {
    this.index = index;
    this.mapper = mapper;
  };

  public int getIndex() {
    return index;
  }

  public static ComplexUnit valueOf(int index) {
    return UNIT_MAP.get(index);
  }

  public List<Object> parse(int raw) {
    return mapper.apply(raw);
  }

}
