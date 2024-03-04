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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.connectorio.addons.binding.bacnet.internal.handler.object.task.Names;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;

public class BACnetSamplerComposer implements SamplerComposer<BACnetPropertySampler> {

  private final BacNetClient client;

  public BACnetSamplerComposer(BacNetClient client) {
    this.client = client;
  }

  @Override
  public List<BACnetPropertySampler> merge(List<BACnetPropertySampler> samplers) {
    if (samplers.size() == 1) {
      return samplers;
    }

    List<Consumer<Map<Sample, Encodable>>> consumers = new ArrayList<>();
    // we start with set as it does not permit duplicates, so any property which would be read twice
    // will be read once
    Map<BacNetObject, Consumer<Map<Sample, Encodable>>> presentValues = new LinkedHashMap<>();
    Map<BacNetObject, Set<String>> mergedSamples = new HashMap<>();

    for (BACnetPropertySampler sampler : samplers) {
      Map<BacNetObject, List<String>> samples = sampler.getSamples();
      for (Entry<BacNetObject, List<String>> entry : samples.entrySet()) {
        for (String property : entry.getValue()) {
          if (Names.PRESENT_VALUE.equals(property)) {
            presentValues.put(entry.getKey(), sampler.getCallback());
          } else {
            if (!mergedSamples.containsKey(entry.getKey())) {
              consumers.add(sampler.getCallback());
              mergedSamples.put(entry.getKey(), new HashSet<>());
            }
            mergedSamples.get(entry.getKey()).addAll(entry.getValue());
          }
        }
      }
    }

    if (presentValues.isEmpty() && !mergedSamples.isEmpty()) {
      return List.of(new BACnetObjectsSampler(client, flattenSamples(mergedSamples), new CompositeCallback(consumers)));
    } else if (!presentValues.isEmpty() && mergedSamples.isEmpty()) {
      return List.of(new BACnetPresentValueSampler(client, presentValues));
    }
    return Arrays.asList(
      new BACnetPresentValueSampler(client, presentValues),
      new BACnetObjectsSampler(client, flattenSamples(mergedSamples), new CompositeCallback(consumers))
    );
  }

  private static Map<BacNetObject, List<String>> flattenSamples(Map<BacNetObject, Set<String>> samples) {
    // align samples input to rely on list of properties
    Map<BacNetObject, List<String>> targetSamples = samples.entrySet().stream()
      .map(entry -> Map.entry(entry.getKey(), new ArrayList<>(entry.getValue())))
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    return targetSamples;
  }

  static class CompositeCallback implements Consumer<Map<Sample, Encodable>> {

    private final List<Consumer<Map<Sample, Encodable>>> consumers;

    CompositeCallback(List<Consumer<Map<Sample, Encodable>>> consumers) {
      this.consumers = consumers;
    }

    @Override
    public void accept(Map<Sample, Encodable> values) {
      for (Consumer<Map<Sample , Encodable>> consumer : consumers) {
        consumer.accept(values);
      }
    }

  }

}
