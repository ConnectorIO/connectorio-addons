/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.connectorio.binding.plc4x.siemens.internal.config;

import org.connectorio.binding.base.config.PollingConfiguration;

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
     * Rack number.
     */
    public Integer rack = 0;

    /**
     * Slot number.
     */
    public Integer slot = 0;

}
