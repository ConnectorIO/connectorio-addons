package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.core.RemoteStartTransactionRequest;
import eu.chargetime.ocpp.model.core.RemoteStopTransactionRequest;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargingCommandHandler {
    private final Logger logger = LoggerFactory.getLogger(ChargingCommandHandler.class);

    public void handle(Command command, ConnectorCommandContext context) {
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

        ocppSender.send(chargerRef, request).whenComplete((confirmation, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to send RemoteStartTransaction with tag {}", remoteStartTag, throwable);
            } else {
                logger.info("RemoteStartTransaction with tag {} sent successfully: {}", remoteStartTag, confirmation);
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