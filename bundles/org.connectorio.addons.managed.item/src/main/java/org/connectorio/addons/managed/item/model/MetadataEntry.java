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
package org.connectorio.addons.managed.item.model;

import java.util.Map;
import java.util.Objects;

public class MetadataEntry {

  private String value;
  private Map<String, Object> config;

  public MetadataEntry() {
  }

  public MetadataEntry(String value, Map<String, Object> config) {
    this.value = value;
    this.config = config;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
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
    if (!(o instanceof MetadataEntry)) {
      return false;
    }
    MetadataEntry that = (MetadataEntry) o;
    return Objects.equals(value, that.value) &&
      Objects.equals(config, that.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, config);
  }
}
