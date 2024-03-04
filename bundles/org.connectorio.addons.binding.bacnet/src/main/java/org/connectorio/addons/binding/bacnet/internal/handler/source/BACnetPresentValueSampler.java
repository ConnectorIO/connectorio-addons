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

import com.serotonin.bacnet4j.type.Encodable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.connectorio.addons.binding.bacnet.internal.handler.object.task.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BACnetPresentValueSampler implements BACnetPropertySampler {
  private final Logger logger = LoggerFactory.getLogger(BACnetPresentValueSampler.class);

  private final BacNetClient client;
  private final Map<BacNetObject, List<String>> samples;
  private final Consumer<List<Object>> callback;

  public BACnetPresentValueSampler(BacNetClient client, Map<BacNetObject, Consumer<Map<Sample, Encodable>>> samples) {
    this.client = client;
    this.samples = samples.keySet().stream()
      .collect(Collectors.toMap(Function.identity(), key -> List.of(Names.PRESENT_VALUE), (l, r) -> l, LinkedHashMap::new));
    this.callback = new PresentValueCallback(new ArrayList<>(samples.keySet()), new ArrayList<>(samples.values()));
  }

  @Override
  public Map<BacNetObject, List<String>> getSamples() {
    return samples;
  }

  @Override
  public Consumer<Map<Sample, Encodable>> getCallback() {
    return null; //callback;
  }

  @Override
  public CompletableFuture<?> fetch() {
    return CompletableFuture.completedFuture(client.getPresentValues(new ArrayList<>(samples.keySet())))
      .whenComplete((result, error) -> {
        if (error != null) {
          logger.debug("Error while fetching present values of {}", samples.keySet(), error);
          return;
        }
        callback.accept(result);
      });
  }

  static class PresentValueCallback implements Consumer<List<Object>> {

    private final List<BacNetObject> objects;
    private final List<Consumer<Map<Sample, Encodable>>> callbacks;

    public PresentValueCallback(List<BacNetObject> objects, List<Consumer<Map<Sample, Encodable>>> callbacks) {
      this.objects = objects;
      this.callbacks = callbacks;
    }

    @Override
    public void accept(List<Object> values) {
      for (int index = 0; index < values.size(); index++) {
        Object value = values.get(index);
        if (value instanceof Encodable) {
          callbacks.get(index).accept(Map.of(new Sample(objects.get(index), Names.PRESENT_VALUE), (Encodable) value));
        }
      }
    }
  }
}
