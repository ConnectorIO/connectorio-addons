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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetClientException;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshDeviceTask extends AbstractTask {

  private final Logger logger = LoggerFactory.getLogger(RefreshDeviceTask.class);
  private final Supplier<CompletableFuture<BacNetClient>> client;
  private final ThingHandlerCallback callback;
  private final Device device;
  //private final Map<BacNetObject, List<Readout>> channels;

  private final Map<BacNetObject, ChannelUID> presentValueReadouts;
  private final List<ChannelUID> presentValueChannels;
  private final Map<BacNetObject, List<String>> customReadouts;
  private final Map<BacNetObject, List<ChannelUID>> customReadoutChannels;

  public RefreshDeviceTask(Supplier<CompletableFuture<BacNetClient>> client, ThingHandlerCallback callback, Device device, Map<BacNetObject, List<Readout>> channels) {
    this.client = client;
    this.callback = callback;
    this.device = device;

    // calculate readouts which are interested only in present values
    presentValueReadouts = new LinkedHashMap<>();
    presentValueChannels = new ArrayList<>();
    customReadouts = new LinkedHashMap<>();
    customReadoutChannels = new LinkedHashMap<>();
    for (Entry<BacNetObject, List<Readout>> entry : channels.entrySet()) {
      List<Readout> readouts = entry.getValue();
      if (readouts.size() == 1 && PropertyIdentifier.presentValue.equals(readouts.get(0).propertyIdentifier)) {
        presentValueReadouts.put(readouts.get(0).object, readouts.get(0).channel);
        presentValueChannels.add(readouts.get(0).channel);
      } else {
        customReadouts.put(entry.getKey(), readouts.stream().map(r -> r.propertyIdentifier.toString()).collect(Collectors.toList()));
        customReadoutChannels.put(entry.getKey(), readouts.stream().map(r -> r.channel).collect(Collectors.toList()));
      }
    }
  }

  @Override
  public void run() {
    CompletableFuture<BacNetClient> clientFuture = client.get();
    if (clientFuture.isDone() && !clientFuture.isCancelled() && !clientFuture.isCompletedExceptionally()) {
      try {
        BacNetClient bacNetClient = clientFuture.get();
        if (bacNetClient == null) {
          return;
        }

        for (BacNetObject object : customReadouts.keySet()) {
          List<String> properties = customReadouts.get(object);
          List<Object> values = bacNetClient.getObjectAttributeValues(object, properties);
          processAnswers(values, customReadoutChannels.get(object));
        }

        fetchPresentValues(bacNetClient);
      } catch (BacNetClientException e) {
        logger.warn("Could not read property {} value. Client reported an error", device, e);
      } catch (InterruptedException | ExecutionException e) {
        logger.debug("Could not complete operation", e);
      } catch (Exception e) {
        logger.debug("Unexpected error while retrieving values from network", e);
      }
    }
  }

  private void fetchPresentValues(BacNetClient bacNetClient) {
    List<Object> values = bacNetClient.getPresentValues(new ArrayList<>(presentValueReadouts.keySet()));
    processAnswers(values, presentValueChannels);
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
