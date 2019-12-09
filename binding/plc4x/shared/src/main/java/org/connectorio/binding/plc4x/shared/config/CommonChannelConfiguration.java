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
package org.connectorio.binding.plc4x.shared.config;

import org.connectorio.binding.base.config.PollingConfiguration;

/**
 * The {@link CommonChannelConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class CommonChannelConfiguration extends PollingConfiguration {

    /**
     * Field to be read from PLC.
     */
    public String field;

}
