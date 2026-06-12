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
package org.connectorio.addons.binding.ocpp.internal.config;

import java.util.List;
import org.connectorio.addons.binding.config.Configuration;
import org.connectorio.addons.binding.ocpp.internal.server.custom.OcularSolarEcoMode.EcoMode;

public class ServerConfig implements Configuration {

  public String address;
  public int port;
  public int heartbeat;
  public EcoMode initialOcularEcoMode = EcoMode.NONE;
  public int pingInterval = 60;

  public int meterValueSampleInterval = 30;
  public String meterValuesData = "Energy.Active.Import.Register,Power.Active.Import,Current.Import,Voltage";
  public int clockAlignedDataInterval = 30;
  public boolean disableRemoteTxAuthorization = true;

  public List<String> chargers;
  public List<String> tags;
}
