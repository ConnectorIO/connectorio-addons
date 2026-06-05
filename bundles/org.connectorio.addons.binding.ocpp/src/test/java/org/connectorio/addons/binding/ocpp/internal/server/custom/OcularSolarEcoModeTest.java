package org.connectorio.addons.binding.ocpp.internal.server.custom;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.junit.jupiter.api.Test;

class OcularSolarEcoModeTest {

  private final OcppSender sender = mock(OcppSender.class);
  private final ChargerReference charger = new ChargerReference("cp");

  @Test
  void doesNotSendWhenDisabled() {
    // NONE is the configured default — the feature is off, nothing should be pushed.
    OcularSolarEcoMode eco = new OcularSolarEcoMode(OcularSolarEcoMode.EcoMode.NONE);
    eco.setOcppSender(sender);
    eco.applyOcularEcoMode(charger);
    verify(sender, never()).send(any(ChargerReference.class), any(Request.class));
  }

  @Test
  void sendsChangeConfigurationForValidMode() {
    when(sender.send(any(ChargerReference.class), any(Request.class)))
        .thenReturn(CompletableFuture.<Confirmation>completedFuture(null));
    OcularSolarEcoMode eco = new OcularSolarEcoMode(OcularSolarEcoMode.EcoMode.SOLAR_ONLY);
    eco.setOcppSender(sender);
    eco.applyOcularEcoMode(charger);
    verify(sender).send(any(ChargerReference.class), any(ChangeConfigurationRequest.class));
  }
}
