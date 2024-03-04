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
import java.util.function.Consumer;
import org.code_house.bacnet4j.wrapper.api.BacNetToJavaConverter;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplerCallback implements Consumer<Encodable> {

  private final Logger logger = LoggerFactory.getLogger(SamplerCallback.class);

  private final BacNetToJavaConverter<State> converter;
  private final Consumer<State> callback;

  public SamplerCallback(BacNetToJavaConverter<State> converter, Consumer<State> callback) {
    this.converter = converter;
    this.callback = callback;
  }

  @Override
  public void accept(Encodable value) {
    if (value != null) {
      State state = converter.fromBacNet(value);
      if (state != null) {
        callback.accept(state);
        return;
      }
      logger.debug("Could not map value {} to valid valid state, ignoring callback {}", value, callback);
    }
  }

}