/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io;

import java.util.concurrent.CompletableFuture;
import javax.measure.Quantity;
import org.connectorio.addons.binding.config.Configuration;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TACanShort;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TACanString;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TACanStringPointer;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.i18n.Language;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;

public abstract class TACanInputObject<T extends Value> extends TACanInputOutputObject<T> {

  private CompletableFuture<InputConfig> config;
  private InputConfig loadedConfig;

  public TACanInputObject(TADevice device, boolean reload, int baseIndex, int index, int unit) {
    super(device, reload, baseIndex, index, unit);
  }

  @Override
  protected void reload() {
    getName().whenComplete((name, failure) -> {
      if (failure != null) {
        return;
      }
      if (device.getLanguage().matches(Language.UNUSED, name)) {
        loadedConfig = new InputConfig(-1);
        config = CompletableFuture.completedFuture(loadedConfig);
        return;
      }

      config = readNode() // text "unused" or number as an text ie. "15"
        .thenCompose(this::readIndex)
        .thenCompose(this::readUnit)
        .whenComplete((result, error) -> {
          if (error != null) {
            logger.debug("Could not determine CAN Input #{} config.", index, error);
            return;
          }
          logger.debug("Determined CAN Input #{} config {}", index, result);
          loadedConfig = result;
        });
    });
  }

  @Override
  public int getIndex() {
    if (loadedConfig != null && loadedConfig.getIndex() != -1) {
      return loadedConfig.getIndex();
    }
    return super.getIndex();
  }

  public int getConfiguredUnit() {
    if (loadedConfig != null) {
     return loadedConfig.getUnit();
    }

    return super.getUnit();
  }

  public CompletableFuture<InputConfig> getConfiguration() {
    return config;
  }

  private CompletableFuture<InputConfig> readNode() {
    return new TACanString(device.getNode(), (short) (baseIndex + 0x10), (short) (index - 1)).toFuture().thenApply((result) -> {
      logger.info("CAN Input #{} listen to node: {}", index, result);
      if (device.getLanguage().matches(Language.UNUSED, result)) {
        return new InputConfig(-1);
      }
      if (result.matches("\\d+")) {
        return new InputConfig(Integer.parseInt(result));
      }
      return new InputConfig(-1);
    });
  }

  private CompletableFuture<InputConfig> readIndex(InputConfig config) {
    if (config.node == -1) {
      return CompletableFuture.completedFuture(config);
    }

    return new TACanShort(device.getNode(), (short) (baseIndex + 0x11), (short) (index - 1)).toFuture().thenApply((result) -> {
      logger.info("CAN Input #{} index: {}", index, result);
      Object value = result.getValue();
      if (value instanceof Number) {
        return new InputConfig(config.node, ((Number) value).intValue());
      } else if (value instanceof Quantity<?>) {
        return new InputConfig(config.node, ((Quantity<?>) value).getValue().intValue());
      }
      return new InputConfig(config.node, -1);
    });
  }

  private CompletableFuture<InputConfig> readUnit(InputConfig config) {
    if (config.node == -1) {
      return CompletableFuture.completedFuture(config);
    }

    return new TACanString(device.getNode(), (short) (baseIndex + 0x13), (short) (index - 1)).toFuture().thenApply((result) -> {
      if (device.getLanguage().matches(Language.AUTOMATIC, result)) {
        logger.info("CAN Input #{} unit: {} (translated to: automatic)", index, result);
        return new InputConfig(config.node, config.index, InputConfig.UNIT_AUTOMATIC);
      }

      if (device.getLanguage().matches(Language.USER_DEFINED, result)) {
        logger.info("CAN Input #{} unit: {} (translated to: user defined)", index, result);
        return new InputConfig(config.node, config.index, InputConfig.UNIT_USER_DEFINED);
      }
      logger.info("CAN Input #{} unit: {} (assumed: user defined)", index, result);
      return new InputConfig(config.node, config.index, InputConfig.UNIT_USER_DEFINED);
    }).thenCompose(configWithUnit -> {
      if (configWithUnit.getUnit() == InputConfig.UNIT_AUTOMATIC) {
        return CompletableFuture.completedFuture(configWithUnit);
      }

      return new TACanString(device.getNode(), (short) (baseIndex + 0x14), (short) (index - 1)).toFuture().thenApply((unitLabel) -> {
        return new InputConfig(configWithUnit.node, configWithUnit.index, parseUnitLabel(unitLabel));
      });
    });
  }

  public String toString() {
    return super.toString() + ", configuredUnit=" + getConfiguredUnit();
  }

  protected abstract int parseUnitLabel(String unitLabel);

  public static class InputConfig {

    public static final Integer UNIT_AUTOMATIC = -1;
    public static final Integer UNIT_USER_DEFINED = -2;
    public static final Integer UNIT_UNKNOWN = -3;

    int node;
    int index;
    int unit;

    public InputConfig(int node) {
      this(node, -1, UNIT_UNKNOWN);
    }

    public InputConfig(int node, int index) {
      this(node, index, UNIT_UNKNOWN);
    }

    public InputConfig(int node, int index, int unit) {
      this.node = node;
      this.index = index;
      this.unit = unit;
    }

    public int getNode() {
      return node;
    }

    public int getIndex() {
      return index;
    }

    public int getUnit() {
      return unit;
    }

    public String toString() {
      return "input [node=" + node + ", index=" + index + ", unit=" + unit + "]";
    }
  }

}
