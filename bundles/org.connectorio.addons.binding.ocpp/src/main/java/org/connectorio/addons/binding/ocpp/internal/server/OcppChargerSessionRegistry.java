package org.connectorio.addons.binding.ocpp.internal.server;

import java.util.UUID;

public interface OcppChargerSessionRegistry {

  UUID getSession(ChargerReference chargerReference);

  ChargerReference removeSession(UUID session);

  ChargerReference getCharger(UUID sessionIndex);
}
