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
package org.connectorio.addons.managed.thing.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ThingEntry {

  private String id;
  private String type;
  private String label;
  private String bridge;
  private Map<String, Object> config;
  private List<ChannelEntry> channels;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getBridge() {
    return bridge;
  }

  public void setBridge(String bridge) {
    this.bridge = bridge;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  public List<ChannelEntry> getChannels() {
    return channels;
  }

  public void setChannels(List<ChannelEntry> channels) {
    this.channels = channels;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ThingEntry)) {
      return false;
    }
    ThingEntry that = (ThingEntry) o;
    return Objects.equals(id, that.id) &&
      Objects.equals(type, that.type) &&
      Objects.equals(label, that.label) &&
      Objects.equals(bridge, that.bridge) &&
      Objects.equals(config, that.config) &&
      Objects.equals(channels, that.channels);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, label, bridge, config, channels);
  }
}
