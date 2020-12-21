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
package org.connectorio.addons.binding.plc4x.siemens.internal.config;

import org.connectorio.addons.binding.config.PollingConfiguration;

/**
 * The {@link SiemensNetworkConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class SiemensNetworkConfiguration extends PollingConfiguration {

    /**
     * Host or IP address which should be used for PLC access.
     */
    public String host;

    /**
     * Local rack number.
     */
    public Integer localRack = 1;

    /**
     * Local slot number.
     */
    public Integer localSlot = 1;

    /**
     * Remote rack number.
     */
    public Integer remoteRack = 0;

    /**
     * Remote slot number.
     */
    public Integer remoteSlot = 0;

    /**
     * PDU size;
     */
    public Integer pduSize = 1024;

    /**
     * Type of controller.
     */
    public ControllerType controllerType;

}
