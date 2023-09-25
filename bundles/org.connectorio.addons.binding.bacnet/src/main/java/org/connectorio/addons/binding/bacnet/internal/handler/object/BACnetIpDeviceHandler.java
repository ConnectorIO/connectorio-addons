/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
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
package org.connectorio.addons.binding.bacnet.internal.handler.object;

import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.device.ip.IpDevice;
import org.connectorio.addons.binding.bacnet.internal.config.IpDeviceConfig;
import org.connectorio.addons.communication.watchdog.WatchdogManager;
import org.connectorio.addons.link.LinkManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

public class BACnetIpDeviceHandler extends BACnetDeviceHandler<IpDeviceConfig> {

  /**
   * Creates a new instance of this class for the {@link Thing}.
   *
   * @param bridge the thing that should be handled, not null
   * @param linkManager manager to track channel links
   * @param watchdogManager communication watchdog
   */
  public BACnetIpDeviceHandler(Bridge bridge, LinkManager linkManager, WatchdogManager watchdogManager) {
    super(bridge, linkManager, watchdogManager);
  }

  @Override
  protected Device createDevice(IpDeviceConfig config, Integer networkNumber) {
    return new IpDevice(config.instance, config.address, config.port, networkNumber);
  }
}
