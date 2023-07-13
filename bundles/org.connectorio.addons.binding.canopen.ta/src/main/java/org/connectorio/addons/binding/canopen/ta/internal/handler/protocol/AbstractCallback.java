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
package org.connectorio.addons.binding.canopen.ta.internal.handler.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.types.PlcResponseCode;

@Deprecated
public abstract class AbstractCallback implements Consumer<PlcSubscriptionEvent> {

  public static byte[] getBytes(PlcReadResponse event, String field) {
    if (event.getResponseCode(field) == PlcResponseCode.OK) {

      List<Byte> bytes = new ArrayList<>(event.getAllBytes(field));
      byte[] value = new byte[bytes.size()];
      for (int index = 0; index < bytes.size(); index++) {
        value[index] = bytes.get(index);
      }

      return value;
    }
    throw new IllegalStateException("Filed " + field + " retrieval failed. Reported code: " + event.getResponseCode(field));
  }

}
