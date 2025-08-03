package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.RegistrationStatus;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.OcppChargerSessionRegistry;

public class BootRegistrationAdapter extends CoreEventHandlerAdapter implements
  OcppChargerSessionRegistry {

  private final Map<UUID, ChargerReference> registrations = new ConcurrentHashMap<>();

  private final Set<String> identifiers;

  public BootRegistrationAdapter(Set<String> identifiers) {
    this.identifiers = identifiers;
  }

  @Override
  public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex, BootNotificationRequest request) {
    ZonedDateTime time = ZonedDateTime.now();
    if (identifiers.isEmpty() || identifiers.contains(request.getChargePointSerialNumber())) {
      registrations.put(sessionIndex, new ChargerReference(request.getChargePointSerialNumber()));
      return new BootNotificationConfirmation(time, 60, RegistrationStatus.Accepted);
    }

    // keep charger connected, but not active
    return new BootNotificationConfirmation(time, 300, RegistrationStatus.Pending);
  }

  @Override
  public UUID getSession(ChargerReference chargerReference) {
    for (Map.Entry<UUID, ChargerReference> entry : registrations.entrySet()) {
      if (entry.getValue().equals(chargerReference)) {
        return entry.getKey();
      }
    }
    return null;
  }

  @Override
  public ChargerReference removeSession(UUID session) {
    return registrations.remove(session);
  }

  @Override
  public ChargerReference getCharger(UUID sessionIndex) {
    return registrations.get(sessionIndex);
  }
}
