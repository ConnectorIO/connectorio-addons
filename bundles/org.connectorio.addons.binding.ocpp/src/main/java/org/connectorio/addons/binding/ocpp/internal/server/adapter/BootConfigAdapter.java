package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.GetConfigurationRequest;
import eu.chargetime.ocpp.model.core.KeyValueType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerConfigRegistry;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootConfigAdapter extends CoreEventHandlerAdapter implements OcppChargerConfigRegistry {

  private final Logger logger = LoggerFactory.getLogger(BootConfigAdapter.class);
  private final Map<ChargerReference, Map<String, Object>> configMap = new ConcurrentHashMap<>();
  private final OcppChargerSessionRegistry sessionRegistry;
  private final OcppSender sender;

  public BootConfigAdapter(OcppChargerSessionRegistry sessionRegistry, OcppSender sender) {
    this.sessionRegistry = sessionRegistry;
    this.sender = sender;
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex, BootNotificationRequest request) {
    ChargerReference reference = new ChargerReference(request.getChargePointSerialNumber());
    if (sessionRegistry.getSession(reference) != null) {
      sender.<GetConfigurationConfirmation>send(reference, new GetConfigurationRequest()).whenComplete((r, e) -> {
        if (e != null) {
          logger.warn("Could not obtain configuration for charger {}. This indicates incomplete implementation of OCPP core profile for charger.", reference, e);
          return;
        }

        Map<String, Object> config = new LinkedHashMap<>();
        KeyValueType[] keys = r.getConfigurationKey();
        for (KeyValueType k : keys) {
          config.put(k.getKey(), k.getValue());
        }
        configMap.put(reference, config);
      });
    }

    return null;
  }

  @Override
  public Map<String, Object> getConfig(ChargerReference reference) {
    return configMap.get(reference);
  }
}
