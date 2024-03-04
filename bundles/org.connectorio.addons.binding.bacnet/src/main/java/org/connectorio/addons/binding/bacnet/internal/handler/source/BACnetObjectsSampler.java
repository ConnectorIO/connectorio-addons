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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BACnetObjectsSampler implements BACnetPropertySampler {
  private final Logger logger = LoggerFactory.getLogger(BACnetObjectsSampler.class);

  private final BacNetClient client;
  private final Map<BacNetObject, List<String>> samples;
  private final Consumer<Map<Sample, Encodable>> callback;

  public BACnetObjectsSampler(BacNetClient client, BacNetObject object, String property, Consumer<Encodable> callback) {
    this(client, Map.of(object, List.of(property)), new SingleValueCallback(callback, new Sample(object, property)));
  }

  public BACnetObjectsSampler(BacNetClient client, Map<BacNetObject, List<String>> samples, Consumer<Map<Sample, Encodable>> callback) {
    this.client = client;
    this.samples = samples;
    this.callback = callback;
  }

  @Override
  public Map<BacNetObject, List<String>> getSamples() {
    return samples;
  }

  @Override
  public Consumer<Map<Sample, Encodable>> getCallback() {
    return callback;
  }

  @Override
  public CompletableFuture<?> fetch() {
    CompletableFuture<Map<Sample, Encodable>> future = null;
    for (Entry<BacNetObject, List<String>> object : samples.entrySet()) {
      if (future == null) {
        future = read(object.getKey(), object.getValue());
      } else {
        future = future.thenCombine(read(object.getKey(), object.getValue()), (known, retrieved) -> {
          known.putAll(retrieved);
          return known;
        });
      }
    }

    if (future == null) {
      return CompletableFuture.failedFuture(new NullPointerException("Sampler brought no results"));
    }

    return future.whenComplete((result, error) -> {
      if (error != null) {
        logger.warn("Could not retrieve");
        return;
      }
      callback.accept(result);
    });
  }

  private CompletableFuture<Map<Sample, Encodable>> read(BacNetObject object, List<String> properties) {
    Map<Sample, Encodable> values = new HashMap<>();
    List<Object> answers = client.getObjectAttributeValues(object, properties);
    for (int index = 0; index < answers.size(); index++) {
      Object value = answers.get(index);
      String property = properties.get(index);
      if (value instanceof Encodable) {
        values.put(new Sample(object, property), (Encodable) value);
      }
    }
    return CompletableFuture.completedFuture(values);
  }

  static class SingleValueCallback implements Consumer<Map<Sample, Encodable>> {

    private final Consumer<Encodable> callback;
    private final Sample sample;

    SingleValueCallback(Consumer<Encodable> callback, Sample sample) {
      this.callback = callback;
      this.sample = sample;
    }

    @Override
    public void accept(Map<Sample, Encodable> result) {
      callback.accept(result.get(sample));
    }
  }
}
