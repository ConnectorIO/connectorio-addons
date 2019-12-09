/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional information.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.connectorio.binding.plc4x.siemens.internal;

import org.connectorio.binding.plc4x.shared.Plc4xBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SiemensBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface SiemensBindingConstants extends Plc4xBindingConstants {

  String BINDING_ID = Plc4xBindingConstants.protocol("s7");

  ThingTypeUID THING_TYPE_TCP_IP = new ThingTypeUID(BINDING_ID, "network");

  ThingTypeUID THING_TYPE_S7 = new ThingTypeUID(BINDING_ID, "s7");

  // List of all Channel types
  String SWITCH = "switch";

  Long DEFAULT_REFRESH_INTERVAL = 1000L;

}
