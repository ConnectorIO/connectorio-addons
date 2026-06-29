package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.core.RemoteStartTransactionRequest;
import eu.chargetime.ocpp.model.core.RemoteStopTransactionRequest;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargingCommandHandler {
    private final Logger logger = LoggerFactory.getLogger(ChargingCommandHandler.class);

    public void handle(Command command, ConnectorCommandContext context) {
        if (command instanceof RefreshType) {
            // charging is a write-only RemoteStart/Stop control; REFRESH has nothing to read.
            return;
        }
        if (!(command instanceof OnOffType)) {
            logger.warn("Unsupported command type for charging control: {}", command.getClass());
            return;
        }

        if (OnOffType.ON.equals(command)) {
            sendRemoteStartTransaction(context);
        } else {
            sendRemoteStopTransaction(context);
        }
    }

    private void sendRemoteStartTransaction(ConnectorCommandContext context) {
        OcppSender ocppSender = context.getOcppSender();
        String chargerSerialNumber = context.getChargerSerialNumber();
        String remoteStartTag = context.getRemoteStartTag();
        if (ocppSender == null || chargerSerialNumber == null) {
            logger.warn("OcppSender or charger serial not set. Cannot send RemoteStartTransaction.");
            return;
        }

        ChargerReference chargerRef = new ChargerReference(chargerSerialNumber);
        RemoteStartTransactionRequest request = new RemoteStartTransactionRequest(remoteStartTag);
        // Target the specific connector. On a multi-connector charge point the
        // connectorId is required — without it some chargers (e.g. Phoenix CHARX)
        // cannot bind the remote start to an EVSE and Reject the request. Single
        // connector chargers are unaffected (connectorId 1 is unambiguous). OCPP
        // requires connectorId > 0 (0 = the charge point as a whole, invalid for
        // RemoteStart), so omit anything else and let the charger choose.
        Integer connectorId = context.getConnectorId();
        if (connectorId != null && connectorId > 0) {
            request.setConnectorId(connectorId);
        }

        ocppSender.send(chargerRef, request).whenComplete((confirmation, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to send RemoteStartTransaction (connector {}, tag {})", connectorId, remoteStartTag,
                        throwable);
            } else {
                logger.info("RemoteStartTransaction (connector {}, tag {}) sent successfully: {}", connectorId,
                        remoteStartTag, confirmation);
            }
        });
    }

    private void sendRemoteStopTransaction(ConnectorCommandContext context) {
        OcppSender ocppSender = context.getOcppSender();
        String chargerSerialNumber = context.getChargerSerialNumber();
        Integer currentTransactionId = context.getCurrentTransactionId();
        if (ocppSender == null || chargerSerialNumber == null) {
            logger.warn("OcppSender or charger serial not set. Cannot send RemoteStopTransaction.");
            return;
        }

        if (currentTransactionId == null) {
            logger.warn("No active transaction found for RemoteStopTransaction.");
            return;
        }

        ChargerReference chargerRef = new ChargerReference(chargerSerialNumber);
        RemoteStopTransactionRequest request = new RemoteStopTransactionRequest(currentTransactionId);

        ocppSender.send(chargerRef, request).whenComplete((confirmation, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to send RemoteStopTransaction for transaction {}", currentTransactionId, throwable);
            } else {
                logger.info("RemoteStopTransaction for transaction {} sent successfully: {}", currentTransactionId, confirmation);
            }
        });
    }
}