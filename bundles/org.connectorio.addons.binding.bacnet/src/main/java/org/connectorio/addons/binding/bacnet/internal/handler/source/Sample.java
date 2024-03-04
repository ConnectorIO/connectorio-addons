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

import java.util.Objects;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;

public class Sample {

  private final BacNetObject object;
  private final String property;

  public Sample(BacNetObject object, String property) {
    this.object = object;
    this.property = property;
  }

  public BacNetObject getObject() {
    return this.object;
  }

  public String getProperty() {
    return this.property;
  }

  @Override
  public boolean equals(Object object) {

    if (this == object) {
      return true;
    }
    if (!(object instanceof Sample)) {
      return false;
    }
    Sample sample = (Sample) object;
    return Objects.equals(getObject(), sample.getObject())
        && Objects.equals(property, sample.property);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getObject(), property);
  }
}
