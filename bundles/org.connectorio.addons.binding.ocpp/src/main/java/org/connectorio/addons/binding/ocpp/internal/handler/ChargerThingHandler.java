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
package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.HeartbeatConfirmation;
import eu.chargetime.ocpp.model.core.HeartbeatRequest;
import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.ocpp.internal.OcppAttendant;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.config.ChargerConfig;
import org.connectorio.addons.binding.ocpp.internal.discovery.OcppChargerConnectorDiscoveryService;
import org.connectorio.addons.binding.ocpp.internal.server.CompositeRequestListener;
import org.connectorio.addons.binding.ocpp.internal.server.listener.HeartbeatHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.MeterValuesHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.StatusNotificationHandler;
import org.connectorio.addons.binding.ocpp.internal.server.listener.TransactionHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargerThingHandler extends GenericBridgeHandlerBase<ChargerConfig> implements
  HeartbeatHandler, StatusNotificationHandler, TransactionHandler, MeterValuesHandler,
  OcppAttendant, OcppSender {

  private final Logger logger = LoggerFactory.getLogger(ConnectorThingHandler.class);
  private final CompositeRequestListener listener = new CompositeRequestListener();
  private final ChargerConnectorAdapter adapter = new ChargerConnectorAdapter(listener);

  public ChargerThingHandler(Bridge bridge) {
    super(bridge);
  }


  @Override
  public void initialize() {
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void dispose() {
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Arrays.asList(OcppChargerConnectorDiscoveryService.class);
  }

  public void handleBoot(BootNotificationRequest request) {
    if (request == null) {
      return;
    }
    java.util.Map<String, String> properties = editProperties();
    if (request.getChargePointVendor() != null) {
      properties.put(org.openhab.core.thing.Thing.PROPERTY_VENDOR, request.getChargePointVendor());
    }
    if (request.getChargePointModel() != null) {
      properties.put(org.openhab.core.thing.Thing.PROPERTY_MODEL_ID, request.getChargePointModel());
    }
    if (request.getFirmwareVersion() != null) {
      properties.put(org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION, request.getFirmwareVersion());
    }
    if (request.getChargePointSerialNumber() != null) {
      properties.put(org.openhab.core.thing.Thing.PROPERTY_SERIAL_NUMBER, request.getChargePointSerialNumber());
    }
    if (request.getChargeBoxSerialNumber() != null) {
      properties.put("chargeBoxSerialNumber", request.getChargeBoxSerialNumber());
    }
    if (request.getIccid() != null) {
      properties.put("iccid", request.getIccid());
    }
    if (request.getImsi() != null) {
      properties.put("imsi", request.getImsi());
    }
    if (request.getMeterType() != null) {
      properties.put("meterType", request.getMeterType());
    }
    if (request.getMeterSerialNumber() != null) {
      properties.put("meterSerialNumber", request.getMeterSerialNumber());
    }
    updateProperties(properties);
  }

  @Override
  public HeartbeatConfirmation handleHeartbeat(HeartbeatRequest request) {
    updateStatus(ThingStatus.ONLINE);
    return new HeartbeatConfirmation(ZonedDateTime.now());
  }

  @Override
  public MeterValuesConfirmation handleMeterValues(MeterValuesRequest request) {
    updateStatus(ThingStatus.ONLINE);
    return adapter.handleMeterValues(request);
  }

  @Override
  public StatusNotificationConfirmation handleStatusNotification(StatusNotificationRequest request) {
    updateStatus(ThingStatus.ONLINE);

    StatusNotificationConfirmation confirmation = adapter.handleStatusNotification(request);

    if (confirmation == null) {
      return new StatusNotificationConfirmation();
    }
    return confirmation;
  }

  @Override
  public StartTransactionConfirmation handleStartTransaction(StartTransactionRequest request) {
    updateStatus(ThingStatus.ONLINE);
    return adapter.handleStartTransaction(request);
  }

  @Override
  public StopTransactionConfirmation handleStopTransaction(StopTransactionRequest request) {
    updateStatus(ThingStatus.ONLINE);
    return adapter.handleStopTransaction(request);
  }

  @Override
  public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
    if (childHandler instanceof ConnectorThingHandler) {
      Integer connectorId = getConnectorId(childThing);
      if (connectorId != null) {
        adapter.addConnector(connectorId, (ConnectorThingHandler) childHandler);
        
        // Pass OcppSender and charger serial to connector for charging profile requests
        Object serial = getThing().getConfiguration().get("serialNumber");
        if (serial instanceof String) {
          ((ConnectorThingHandler) childHandler).setOcppSender(this, (String) serial);
        }
      }
    }
  }

  @Override
  public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
    Integer connectorId = getConnectorId(childThing);
    if (connectorId != null) {
      adapter.removeConnector(connectorId);
    }
  }

  @Override
  public <T extends Request> boolean addRequestListener(Class<T> type, OcppRequestListener<T> listener) {
    return this.listener.addRequestListener(type, listener);
  }

  @Override
  public <T extends Request> void removeRequestListener(OcppRequestListener<T> listener) {
    this.listener.removeRequestListener(listener);
  }

  @Override
  public CompletionStage<Confirmation> send(ChargerReference chargerReference, Request request) {
    Bridge bridge = getBridge();
    if (bridge != null && bridge.getHandler() instanceof ServerBridgeHandler) {
      ServerBridgeHandler serverHandler = (ServerBridgeHandler) bridge.getHandler();
      return serverHandler.send(chargerReference, request);
    }
    throw new RuntimeException("No server bridge handler available");
  }

  private Integer getConnectorId(Thing childThing) {
    Object connectorId = childThing.getConfiguration().get("connectorId");
    if (connectorId instanceof Number) {
      return ((Number) connectorId).intValue();
    }
    if (connectorId instanceof String) {
      try {
        return Integer.parseInt((String) connectorId);
      } catch (NumberFormatException e) {
        logger.warn("Invalid format of connectorId config parameter {}", connectorId, e);
        return null;
      }
    }
    return null;
  }

}
