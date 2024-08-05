package org.connectorio.addons.binding.ocpp.internal.discovery;

import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import java.util.Collections;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.connectorio.addons.binding.ocpp.internal.handler.ChargerThingHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;

import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

public class OcppChargerConnectorDiscoveryService extends AbstractDiscoveryService implements DiscoveryService,
  ThingHandlerService, OcppRequestListener<StatusNotificationRequest> {

  private ChargerThingHandler thingHandler;

  public OcppChargerConnectorDiscoveryService() {
    super(Collections.singleton(OcppBindingConstants.CONNECTOR_THING_TYPE), 30, true);
  }

  @Override
  protected void startScan() {
  }

  @Override
  public void setThingHandler(ThingHandler handler){
    if (handler instanceof ChargerThingHandler) {
      thingHandler = (ChargerThingHandler) handler;
      thingHandler.addRequestListener(StatusNotificationRequest.class, this);
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
  public void onRequest(StatusNotificationRequest request) {
    ThingUID bridgeUid = thingHandler.getThing().getUID();

    Integer connectorId = request.getConnectorId();
    if (connectorId != null && connectorId > 0) {
      // connectorId = 0 indicates status of charge point controller
      ThingUID thingUID = new ThingUID(OcppBindingConstants.CONNECTOR_THING_TYPE, bridgeUid, "" + connectorId);
      DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(thingUID)
        .withBridge(bridgeUid)
        .withProperty("connectorId", connectorId)
        .withRepresentationProperty("connectorId")
        .withLabel("Connector #" + request.getConnectorId());
      thingDiscovered(resultBuilder.build());
    }
  }
}