/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
 */
package org.connectorio.binding.plc4x.shared.handler;

import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.GenericThingHandler;
import org.connectorio.binding.base.handler.polling.PollingThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

public interface Plc4xThingHandler<T extends PlcConnection, B extends Plc4xBridgeHandler<T, ?>, C extends PollingConfiguration>
  extends ThingHandler, GenericThingHandler<B, C>, PollingThingHandler<B, C> {

}
