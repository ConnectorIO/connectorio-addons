package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.core.ChargingProfile;
import eu.chargetime.ocpp.model.core.ChargingProfileKindType;
import eu.chargetime.ocpp.model.core.ChargingProfilePurposeType;
import eu.chargetime.ocpp.model.core.ChargingRateUnitType;
import eu.chargetime.ocpp.model.core.ChargingSchedule;
import eu.chargetime.ocpp.model.core.ChargingSchedulePeriod;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargeLimitCommandHandler {
    private final Logger logger = LoggerFactory.getLogger(ChargeLimitCommandHandler.class);

    public void handle(Command command, ConnectorCommandContext context) {
        double limit;
        if (command instanceof DecimalType) {
            limit = ((DecimalType) command).doubleValue();
        } else if (command instanceof QuantityType) {
            limit = ((QuantityType<?>) command).doubleValue();
        } else {
            logger.warn("Unsupported command type for chargeLimit: {}", command.getClass());
            return;
        }
        sendChargingProfile(limit, context.getOcppSender(), context.getChargerSerialNumber(), context.getConnectorId());
    }

    private void sendChargingProfile(double limit, OcppSender ocppSender, String chargerSerialNumber, Integer connectorId) {
        if (ocppSender == null || chargerSerialNumber == null || connectorId == null) {
            logger.warn("OcppSender, charger serial or connector id not set. Cannot send charging profile.");
            return;
        }

        ChargerReference chargerRef = new ChargerReference(chargerSerialNumber);

        // Create charging profile with the specified limit
        ChargingSchedulePeriod period = new ChargingSchedulePeriod(0, limit);
        ChargingSchedule schedule = new ChargingSchedule(
            ChargingRateUnitType.A,
            new ChargingSchedulePeriod[]{ period }
        );

        ChargingProfile profile = new ChargingProfile();
        profile.setChargingProfileId(1);
        profile.setStackLevel(0);
        profile.setChargingProfilePurpose(ChargingProfilePurposeType.TxDefaultProfile);
        profile.setChargingProfileKind(ChargingProfileKindType.Relative);
        profile.setChargingSchedule(schedule);

        SetChargingProfileRequest setProfileRequest = new SetChargingProfileRequest(connectorId, profile);
        
        ocppSender.send(chargerRef, setProfileRequest).whenComplete((confirmation, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to send SetChargingProfile with limit {}", limit, throwable);
            } else {
                logger.info("SetChargingProfile with limit {} sent successfully: {}", limit, confirmation);
            }
        });
    }
}