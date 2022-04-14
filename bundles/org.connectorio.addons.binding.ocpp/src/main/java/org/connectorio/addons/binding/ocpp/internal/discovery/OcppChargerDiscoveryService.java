/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.ocpp.internal.discovery;

import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import java.util.Collections;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.connectorio.addons.binding.ocpp.internal.handler.ServerBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

public class OcppChargerDiscoveryService extends AbstractDiscoveryService implements DiscoveryService,
  ThingHandlerService, OcppRequestListener<BootNotificationRequest> {

  private ServerBridgeHandler thingHandler;

  public OcppChargerDiscoveryService() {
    super(Collections.singleton(OcppBindingConstants.CHARGER_THING_TYPE), 30, true);
  }

  @Override
  protected void startScan() {
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof ServerBridgeHandler) {
      thingHandler = (ServerBridgeHandler) handler;
      thingHandler.addRequestListener(BootNotificationRequest.class, this);
    }
  }

  @Override
  public ThingHandler getThingHandler() {
    return thingHandler;
  }

  @Override
  public void activate() {
    ThingHandlerService.super.activate();
  }

  @Override
  public void deactivate() {
    thingHandler.removeRequestListener(this);
    ThingHandlerService.super.deactivate();
  }

  @Override
  public void onRequest(BootNotificationRequest request) {
    ThingUID bridgeUid = thingHandler.getThing().getUID();
    String thingId = request.getChargePointSerialNumber().replaceAll("[^A-Z0-9_]", "").toLowerCase();
    ThingUID thingUID = new ThingUID(OcppBindingConstants.CHARGER_THING_TYPE, bridgeUid, thingId);
    DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(thingUID)
      .withBridge(bridgeUid)
      .withProperty(Thing.PROPERTY_MODEL_ID, request.getChargePointModel())
      .withProperty(Thing.PROPERTY_SERIAL_NUMBER, request.getMeterSerialNumber())
      .withProperty(Thing.PROPERTY_VENDOR, request.getChargePointVendor())
      .withProperty(Thing.PROPERTY_FIRMWARE_VERSION, request.getFirmwareVersion())
      .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
      .withLabel(request.getChargePointVendor() + " " + request.getChargePointModel() + " serial no. " + request.getChargePointSerialNumber());
    thingDiscovered(resultBuilder.build());
  }

}
