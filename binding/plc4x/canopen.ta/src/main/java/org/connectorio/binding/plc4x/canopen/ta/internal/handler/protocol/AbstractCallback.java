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
package org.connectorio.binding.plc4x.canopen.ta.internal.handler.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;

public abstract class AbstractCallback implements Consumer<PlcSubscriptionEvent> {

  public static byte[] getBytes(PlcSubscriptionEvent event, String field) {
    List<Byte> bytes = new ArrayList<>(event.getAllBytes(field));
    byte[] value = new byte[bytes.size()];
    for (int index = 0; index < bytes.size(); index++) {
      value[index] = bytes.get(index);
    }

    return value;
  }

}
