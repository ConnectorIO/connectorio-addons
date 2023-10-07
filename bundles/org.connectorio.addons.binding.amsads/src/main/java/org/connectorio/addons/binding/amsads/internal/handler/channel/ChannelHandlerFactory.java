/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.amsads.internal.handler.channel;

import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;

public interface ChannelHandlerFactory {

  AdsChannelHandler create(Thing thing, SymbolEntry symbol);

  AdsChannelHandler map(Thing thing, Channel channel);

}
