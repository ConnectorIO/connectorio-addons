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

import eu.chargetime.ocpp.model.Request;
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
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.ocpp.internal.OcppAttendant;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
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
  OcppAttendant {

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
