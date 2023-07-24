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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.plc4x.java.api.messages.PlcTagResponse;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.types.PlcResponseCode;

public class CoRecordReader extends AbstractReader<PlcReadResponse, byte[]> {

  public CoRecordReader(String field) {
    super(field);
  }

  @Override
  protected byte[] extract(PlcReadResponse response, String field) {
    List<Byte> bytes = new ArrayList<>(response.getAllBytes(field));
    byte[] value = new byte[bytes.size()];
    for (int index = 0; index < bytes.size(); index++) {
      value[index] = bytes.get(index);
    }

    return value;
  }

}
