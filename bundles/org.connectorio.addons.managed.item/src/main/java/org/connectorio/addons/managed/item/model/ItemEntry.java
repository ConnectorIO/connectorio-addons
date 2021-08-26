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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ItemEntry {

  private String name;
  private String type;

  private List<String> groups;
  private Set<String> tags;
  private String label;
  private String category;

  private Map<String, MetadataEntry> metadata;
  private Set<LinkEntry> channels;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Map<String, MetadataEntry> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, MetadataEntry> metadata) {
    this.metadata = metadata;
  }

  public Set<LinkEntry> getChannels() {
    return channels;
  }

  public void setChannels(Set<LinkEntry> channels) {
    this.channels = channels;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ItemEntry)) {
      return false;
    }
    ItemEntry entry = (ItemEntry) o;
    return Objects.equals(name, entry.name) &&
      Objects.equals(type, entry.type) &&
      Objects.equals(groups, entry.groups) &&
      Objects.equals(tags, entry.tags) &&
      Objects.equals(label, entry.label) &&
      Objects.equals(category, entry.category) &&
      Objects.equals(metadata, entry.metadata) &&
      Objects.equals(channels, entry.channels);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, groups, tags, label, category, metadata, channels);
  }
}
