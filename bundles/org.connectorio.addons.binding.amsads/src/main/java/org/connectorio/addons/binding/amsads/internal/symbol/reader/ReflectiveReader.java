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
package org.connectorio.addons.binding.amsads.internal.symbol.reader;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.plc4x.java.ads.configuration.AdsConfiguration;
import org.apache.plc4x.java.ads.protocol.AdsProtocolLogic;
import org.apache.plc4x.java.ads.readwrite.AdsDataTypeTableEntry;
import org.apache.plc4x.java.ads.readwrite.AmsPacket;
import org.apache.plc4x.java.ads.readwrite.AmsTCPPacket;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.spi.ConversationContext;
import org.apache.plc4x.java.spi.transaction.RequestTransactionManager;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReader;

public abstract class ReflectiveReader implements SymbolReader {

  protected final ConversationContext<AmsTCPPacket> conversation;
  protected final Map<String, AdsDataTypeTableEntry> dataTypeTable;
  protected final RequestTransactionManager tm;
  protected final AtomicLong invokeId;
  protected final AdsConfiguration configuration;

  protected ReflectiveReader(PlcConnection connection) {
    AdsProtocolLogic logic = getField(connection, "protocol", AdsProtocolLogic.class);
    dataTypeTable = getField(logic, "dataTypeTable", Map.class);
    configuration = getField(logic, "configuration", AdsConfiguration.class);
    conversation = getField(logic, "context", ConversationContext.class);
    tm = getField(logic, "tm", RequestTransactionManager.class);
    invokeId = getField(logic, "invokeIdGenerator", AtomicLong.class);
  }

  protected long getInvokeId() {
    return invokeId.incrementAndGet();
  }

  protected <T> T getField(Object object, String field, Class<T> type) {
    Class<?> clazz = object.getClass();
    do {
      try {
        Field declaredField = clazz.getDeclaredField(field);
        if (!declaredField.isAccessible()) {
          declaredField.setAccessible(true);
        }
        Object value = declaredField.get(object);
        if (type.isInstance(value)) {
          return type.cast(value);
        }
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      } catch (Exception e) {
        return null;
      }
    } while (clazz != Object.class);
    return null;
  }

}
