/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.ocpp.internal.server;

import eu.chargetime.ocpp.model.Request;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.binding.ocpp.internal.OcppAttendant;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;

public class CompositeRequestListener implements OcppRequestListener<Request>, OcppAttendant {

  private Map<Class<?>, Set<OcppRequestListener<Request>>> listeners;

  public CompositeRequestListener() {
    this(new ConcurrentHashMap<>());
  }


  public CompositeRequestListener(Map<Class<?>, Set<OcppRequestListener<Request>>> listeners) {
    this.listeners = listeners;
  }

  @Override
  public void onRequest(Request request) {
    if (listeners.containsKey(request.getClass())) {
      Set<OcppRequestListener<Request>> listeners = this.listeners.get(request.getClass());
      for (OcppRequestListener<Request> listener : listeners) {
        listener.onRequest(request);
      }
    }
  }

  @Override
  public <T extends Request> boolean addRequestListener(Class<T> type, OcppRequestListener<T> listener) {
    if (!listeners.containsKey(type)) {
      listeners.put(type, new LinkedHashSet<>());
    }
    return this.listeners.get(type).add((OcppRequestListener<Request>) listener);
  }

  @Override
  public <T extends Request> void removeRequestListener(OcppRequestListener<T> listener) {
    for (Set<OcppRequestListener<Request>> listenerSet : listeners.values()) {
      listenerSet.remove(listener);
    }
  }
}
