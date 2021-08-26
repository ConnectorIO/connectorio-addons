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

import java.util.Map;
import java.util.Objects;
import org.openhab.core.thing.type.ChannelTypeUID;

public class ChannelEntry {

  private String id;
  private String type;
  private String label;
  private Map<String, Object> config;

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

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChannelEntry)) {
      return false;
    }
    ChannelEntry that = (ChannelEntry) o;
    return Objects.equals(id, that.id) &&
      Objects.equals(type, that.type) &&
      Objects.equals(label, that.label) &&
      Objects.equals(config, that.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, label, config);
  }
}
