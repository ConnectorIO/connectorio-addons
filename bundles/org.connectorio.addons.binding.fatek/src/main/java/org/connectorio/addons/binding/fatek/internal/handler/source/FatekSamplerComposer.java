/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.fatek.internal.handler.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;

/**
 * Composer which merges individual register scanners into one larger one which rely on read mix data
 * command.
 */
public class FatekSamplerComposer implements SamplerComposer<FatekSampler> {

  private final FaconConnection connection;
  private final int stationNumber;

  public FatekSamplerComposer(FaconConnection connection, int stationNumber) {
    this.connection = connection;
    this.stationNumber = stationNumber;
  }

  @Override
  public List<FatekSampler> merge(List<FatekSampler> samplers) {
    if (samplers.size() == 1) {
      return samplers;
    }

    List<Reg> registers = new ArrayList<>();
    List<WrappedCallback> callbacks = new ArrayList<>();

    for (FatekSampler sampler : samplers) {
      List<Reg> samplerRegisters = sampler.getRegisters();
      registers.addAll(samplerRegisters);
      callbacks.add(new WrappedCallback(samplerRegisters, sampler.getCallback()));
    }

    return Collections.singletonList(new FatekRegisterSampler(
      connection, stationNumber, registers, new CompositeCallback(callbacks)
    ));
  }

  static class CompositeCallback implements Consumer<Map<Reg, RegValue>> {

    private final List<WrappedCallback> consumers;

    public CompositeCallback(List<WrappedCallback> consumers) {
      this.consumers = consumers;
    }

    public void accept(Map<Reg, RegValue> result) {
      int index = 0;
      while (index < result.size()) {
        // get subset of registers needed by given sampler
        WrappedCallback callback = consumers.get(index++);
        Map<Reg, RegValue> values = new LinkedHashMap<>();
        for (Reg register : callback.registers) {
          values.put(register, result.get(register));
        }
        callback.accept(values);
      }
    }
  }

  static class WrappedCallback implements Consumer<Map<Reg, RegValue>> {

    private final List<Reg> registers;
    private final Consumer<Map<Reg, RegValue>> callback;

    public WrappedCallback(List<Reg> registers, Consumer<Map<Reg, RegValue>> callback) {
      this.registers = registers;
      this.callback = callback;
    }

    @Override
    public void accept(Map<Reg, RegValue> result) {
      this.callback.accept(result);
    }
  }

}