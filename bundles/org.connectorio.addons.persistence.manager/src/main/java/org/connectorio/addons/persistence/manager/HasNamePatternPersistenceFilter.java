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
package org.connectorio.addons.persistence.manager;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.filter.PersistenceFilter;

public class HasNamePatternPersistenceFilter extends PersistenceFilter implements Predicate<Item> {

  private final Pattern pattern;

  public HasNamePatternPersistenceFilter(String pattern) {
    super("name");
    this.pattern = Pattern.compile(pattern);
  }

  @Override
  public boolean apply(Item item) {
    return test(item);
  }

  @Override
  public void persisted(Item item) {

  }

  @Override
  public boolean test(Item item) {
    return pattern.matcher(item.getName()).matches();
  }

  @Override
  public String toString() {
    return "HasName [" + pattern + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof HasNamePatternPersistenceFilter)) {
      return false;
    }
    HasNamePatternPersistenceFilter that = (HasNamePatternPersistenceFilter) o;
    return Objects.equals(getPattern(), that.getPattern());
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern);
  }

  public String getPattern() {
    return pattern.pattern();
  }

}
