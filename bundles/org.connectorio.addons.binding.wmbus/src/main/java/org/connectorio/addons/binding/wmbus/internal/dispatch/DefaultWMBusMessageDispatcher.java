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
package org.connectorio.addons.binding.wmbus.internal.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageListener;
import org.openmuc.jmbus.HexUtils;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWMBusMessageDispatcher implements WMBusMessageDispatcher {
  private final Logger logger = LoggerFactory.getLogger(DefaultWMBusMessageDispatcher.class);

  private final Map<SecondaryAddress, List<WMBusMessageListener>> listenrMap = new ConcurrentHashMap<>();

  @Override
  public void dispatch(WMBusMessage message) {
    dispatch(WMBusMessageListener.WILDCARD_ADDRESS, message);
    dispatch(message.getSecondaryAddress(), message);
  }

  @Override
  public void attach(WMBusMessageListener listener) {
    SecondaryAddress address = listener.getAddress();

    if (!listenrMap.containsKey(address)) {
      listenrMap.put(address, new ArrayList<>());
    }
    logger.info("Registered message listener for address {}", HexUtils.bytesToHex(address.asByteArray()));
    listenrMap.get(address).add(listener);
  }

  @Override
  public void detach(WMBusMessageListener listener) {
    SecondaryAddress address = listener.getAddress();

    if (listenrMap.containsKey(address)) {
      List<WMBusMessageListener> listeners = listenrMap.get(address);
      if (listeners.remove(listener)) {
        logger.info("Removed message listener for address {}", HexUtils.bytesToHex(address.asByteArray()));
        if (listeners.isEmpty()) {
          // flush key
          listenrMap.remove(address);
        }
      }
    }
  }

  private void dispatch(SecondaryAddress address, WMBusMessage message) {
    List<WMBusMessageListener> listeners = listenrMap.get(address);
    if (listeners != null && !listeners.isEmpty()) {
      for (WMBusMessageListener listener : listeners) {
        listener.onMessage(message);
      }
    }
  }
}
