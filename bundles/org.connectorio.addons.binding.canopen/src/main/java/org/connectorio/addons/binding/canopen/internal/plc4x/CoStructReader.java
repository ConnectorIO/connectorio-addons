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
package org.connectorio.addons.binding.canopen.internal.plc4x;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.spi.values.PlcStruct;

public class CoStructReader extends AbstractReader<PlcReadResponse, PlcStruct> {

  public CoStructReader(String field) {
    super(field);
  }

  @Override
  protected PlcStruct extract(PlcReadResponse response, String field) {
    PlcValue value = response.getPlcValue(field);
    if (value == null) {
      return null;
    }

    if (value instanceof PlcStruct) {
      return (PlcStruct) value;
    }
    throw new IllegalStateException("Could not extract value of field " + field + ". Result is " + value.getClass());
  }
}