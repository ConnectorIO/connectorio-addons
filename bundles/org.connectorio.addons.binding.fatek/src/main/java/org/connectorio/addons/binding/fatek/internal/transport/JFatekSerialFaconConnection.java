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
package org.connectorio.addons.binding.fatek.internal.transport;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.simplify4u.jfatek.FatekPLC;
import org.simplify4u.jfatek.io.FatekIOException;

public class JFatekSerialFaconConnection extends JFatekFaconConnection {

  public JFatekSerialFaconConnection(ExecutorService executor, String port, int connectionTimeout, Map<String, Object> params) throws FatekIOException {
    super(executor, new FatekPLC("serial://" + port + "?plcId=0&timeout=" + connectionTimeout + "&" + parameters(params)));
  }

  private static String parameters(Map<String, Object> params) {
    return params.entrySet().stream()
      .map(e -> e.getKey() + "=" + e.getValue())
      .reduce("", (l, r) -> l + "&" + r);
  }

}
