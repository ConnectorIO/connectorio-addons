package org.connectorio.addons.binding.ocpp.internal.handler;

import org.connectorio.addons.binding.ocpp.internal.OcppSender;

public interface ConnectorCommandContext {
    OcppSender getOcppSender();
    String getChargerSerialNumber();
    String getRemoteStartTag();
    Integer getCurrentTransactionId();
    Integer getConnectorId();
}