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
package org.connectorio.addons.binding.bacnet.internal.handler.object.task;

import com.serotonin.bacnet4j.type.Encodable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetClientException;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadObjectTask extends AbstractTask {

  private final Logger logger = LoggerFactory.getLogger(ReadObjectTask.class);
  private final Supplier<CompletableFuture<BacNetClient>> client;
  private final ThingHandlerCallback callback;
  private final BacNetObject object;
  private final Map<String, ChannelUID> channels;

  public ReadObjectTask(Supplier<CompletableFuture<BacNetClient>> client, ThingHandlerCallback callback, BacNetObject object, Set<ChannelUID> channels) {
    this.client = client;
    this.callback = callback;
    this.object = object;
    this.channels = channels.stream()
      .collect(Collectors.toMap(
        channel -> Names.dashed(channel.getId()),
        Function.identity()
      ));
  }

  @Override
  public void run() {
    CompletableFuture<BacNetClient> clientFuture = client.get();
    if (clientFuture.isDone() && !clientFuture.isCancelled() && !clientFuture.isCompletedExceptionally()) {
      try {
        final List<String> identifiers = new ArrayList<>(channels.keySet());
        Optional.ofNullable(clientFuture.get())
          .map(connection -> connection.getObjectAttributeValues(object, identifiers))
          .ifPresent(states -> {
            int index = 0;
            for (Entry<String, ChannelUID> entry : channels.entrySet()) {
              Object state = states.get(index++);
              State channelState = UnDefType.UNDEF;
              if (state instanceof Encodable) {
                channelState = fromBacNet((Encodable) state);
              }

              logger.debug("Retrieved state for property {} attribute {}: {}", object, entry, channelState);
              callback.stateUpdated(entry.getValue(), channelState);
            }
          });
      } catch (BacNetClientException e) {
        logger.warn("Could not read property {} value. Client reported an error", object, e);
      } catch (InterruptedException | ExecutionException e) {
        logger.debug("Could not complete operation", e);
      }
    }
  }

}
