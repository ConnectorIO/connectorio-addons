/*
 * Copyright (C) 2019-2020 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.binding.bacnet.internal.handler;

import com.serotonin.bacnet4j.obj.BACnetObject;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.binding.bacnet.internal.config.BACnetConfig;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.connectorio.binding.base.handler.polling.common.BasePollingBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;

public abstract class BACnetObjectHandler<T extends BACnetObject, B extends BACnetNetworkBridgeHandler<?>, C extends BACnetConfig>
  extends BasePollingBridgeHandler<C> {

  public BACnetObjectHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

}
