/*
 * Copyright (C) 2024-2024 ConnectorIO sp. z o.o.
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
package org.connectorio.addons.binding.bacnet.internal.handler.source;

import java.util.function.Consumer;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

public class ChannelCallback implements Consumer<State> {

  private final ThingHandlerCallback callback;
  private final Channel channel;

  public ChannelCallback(ThingHandlerCallback callback, Channel channel) {
    this.callback = callback;
    this.channel = channel;
  }

  @Override
  public void accept(State state) {
    callback.stateUpdated(channel.getUID(), state);
  }

  @Override
  public String toString() {
    return "ChannelCallback [" + channel.getUID() + "]";
  }

}