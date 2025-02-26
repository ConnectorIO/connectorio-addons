package org.connectorio.addons.binding.ocpp.internal.server;

import java.util.Map;

public interface OcppChargerConfigRegistry {

  Map<String, Object> getConfig(ChargerReference reference);

}
