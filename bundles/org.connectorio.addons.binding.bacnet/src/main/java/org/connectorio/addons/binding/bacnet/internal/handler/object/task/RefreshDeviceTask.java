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
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetClientException;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.connectorio.addons.link.LinkManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshDeviceTask extends AbstractTask {

  private final Logger logger = LoggerFactory.getLogger(RefreshDeviceTask.class);
  private final Supplier<CompletableFuture<BacNetClient>> client;
  private final Thing thing;
  private final ThingHandlerCallback callback;
  private final Device device;
  private Set<Readout> channels;
  private final LinkManager linkManager;

  public RefreshDeviceTask(Supplier<CompletableFuture<BacNetClient>> client, Thing thing, ThingHandlerCallback callback, Device device, Set<Readout> channels, LinkManager linkManager) {
    this.client = client;
    this.thing = thing;
    this.callback = callback;
    this.device = device;
    this.channels = channels;
    this.linkManager = linkManager;
  }

  private Map<BacNetObject, Set<Readout>> getReadouts() {
    Map<BacNetObject, Set<Readout>> readouts = new LinkedHashMap<>();
    for (Readout readout : channels) {
      if (!linkManager.isLinked(readout.channel)) {
        continue;
      }

      if (!readouts.containsKey(readout.object)) {
        readouts.put(readout.object, new LinkedHashSet<>());
      }
      readouts.get(readout.object).add(readout);
    }
    return readouts;
  }

  @Override
  public void run() {
    if (linkManager.hasLinkedChannels(thing)) {
      logger.trace("Ignore device {} readout, no linked channels found.", device);
      return;
    }

    CompletableFuture<BacNetClient> clientFuture = client.get();
    if (clientFuture.isDone() && !clientFuture.isCancelled() && !clientFuture.isCompletedExceptionally()) {
      try {
        BacNetClient bacNetClient = clientFuture.get();
        if (bacNetClient == null) {
          return;
        }

        Map<BacNetObject, Set<Readout>> readouts = getReadouts();
        Map<BacNetObject, ChannelUID> presentValueReadouts = new LinkedHashMap<>();
        for (Entry<BacNetObject, Set<Readout>> entry : readouts.entrySet()) {
          Readout readout;
          if (entry.getValue().size() == 1 && (readout = entry.getValue().iterator().next()).propertyIdentifier == PropertyIdentifier.presentValue) {
            presentValueReadouts.put(readout.object, readout.channel);
          } else {
            List<String> propertyIdentifiers = entry.getValue().stream()
              .map(r -> r.propertyIdentifier.toString())
              .collect(Collectors.toList());
            List<ChannelUID> channelIds = entry.getValue().stream()
              .map(r -> r.channel)
              .collect(Collectors.toList());
            List<Object> answers = bacNetClient.getObjectAttributeValues(entry.getKey(), propertyIdentifiers);
            processAnswers(answers, channelIds);
          }
        }

        List<Object> values = bacNetClient.getPresentValues(new ArrayList<>(presentValueReadouts.keySet()));
        processAnswers(values, new ArrayList<>(presentValueReadouts.values()));
      } catch (BacNetClientException e) {
        logger.warn("Could not read property {} value. Client reported an error", device, e);
      } catch (InterruptedException | ExecutionException e) {
        logger.debug("Could not complete operation", e);
      } catch (Exception e) {
        logger.debug("Unexpected error while retrieving values from network", e);
      }
    }
  }

  private void processAnswers(List<Object> values, List<ChannelUID> channels) {
    for (int index = 0; index < values.size(); index++) {
      Object value = values.get(index);
      if (value instanceof Encodable) {
        State state = fromBacNet((Encodable) value);

        ChannelUID channel = channels.get(index);
        logger.debug("Retrieved state for property {} attribute {}: {}", device, channel, state);
        callback.stateUpdated(channel, state);
      }
    }
  }

}
