package org.connectorio.addons.binding.ocpp.internal.server;

import eu.chargetime.ocpp.feature.profile.ClientSmartChargingEventHandler;
import eu.chargetime.ocpp.model.smartcharging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSmartChargingHandler implements ClientSmartChargingEventHandler {
	private static final Logger logger = LoggerFactory.getLogger(ServerSmartChargingHandler.class);
	
	@Override
	public SetChargingProfileConfirmation handleSetChargingProfileRequest(SetChargingProfileRequest request) {
	  logger.info("Received SetChargingProfileRequest for connectorId={}: {}", 
	              request.getConnectorId(), request);
	  return new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted);
	}
	
	@Override
	public ClearChargingProfileConfirmation handleClearChargingProfileRequest(ClearChargingProfileRequest request) {
    logger.info("Received ClearChargingProfileRequest: {}", request);
    return new ClearChargingProfileConfirmation(ClearChargingProfileStatus.Accepted);
	}
	
	@Override
	public GetCompositeScheduleConfirmation handleGetCompositeScheduleRequest(GetCompositeScheduleRequest request) {
    logger.info("Received GetCompositeScheduleRequest: {}", request);
    return new GetCompositeScheduleConfirmation(GetCompositeScheduleStatus.Rejected);
	}
}
