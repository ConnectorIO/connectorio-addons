package org.connectorio.addons.binding.ocpp.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.ChargingProfile;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.library.types.StringType;

@ExtendWith(MockitoExtension.class)
class ChargeLimitCommandHandlerTest {

  @Mock
  private OcppSender sender;

  @Mock
  private ConnectorCommandContext context;

  private ChargeLimitCommandHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ChargeLimitCommandHandler();
  }

  @Test
  void shouldSendSetChargingProfileForDecimalType() {
    // given
    when(context.getOcppSender()).thenReturn(sender);
    when(context.getChargerSerialNumber()).thenReturn("charger-serial");
    when(context.getConnectorId()).thenReturn(2);
    when(sender.send(any(ChargerReference.class), any(Request.class)))
      .thenReturn(CompletableFuture.completedFuture(null));

    // when
    handler.handle(new DecimalType(10), context);

    // then
    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sender).send(any(ChargerReference.class), requestCaptor.capture());

    assertThat(requestCaptor.getValue()).isInstanceOf(SetChargingProfileRequest.class);
    assertThat(((SetChargingProfileRequest) requestCaptor.getValue()).getConnectorId()).isEqualTo(2);
  }

  @Test
  void shouldSendSetChargingProfileForQuantityType() {
    // given
    when(context.getOcppSender()).thenReturn(sender);
    when(context.getChargerSerialNumber()).thenReturn("charger-serial");
    when(context.getConnectorId()).thenReturn(1);
    when(sender.send(any(ChargerReference.class), any(Request.class)))
      .thenReturn(CompletableFuture.completedFuture(null));

    QuantityType<?> command = new QuantityType<>(10.0, Units.AMPERE);

    // when
    handler.handle(command, context);

    // then
    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sender).send(any(ChargerReference.class), requestCaptor.capture());

    assertThat(requestCaptor.getValue()).isInstanceOf(SetChargingProfileRequest.class);
    assertThat(((SetChargingProfileRequest) requestCaptor.getValue()).getConnectorId()).isEqualTo(1);
  }

  @Test
  void shouldNotSendWhenConnectorIdMissing() {
    // given
    when(context.getOcppSender()).thenReturn(sender);
    when(context.getChargerSerialNumber()).thenReturn("charger-serial");
    when(context.getConnectorId()).thenReturn(null);

    // when
    handler.handle(new DecimalType(10), context);

    // then
    verify(sender, never()).send(any(ChargerReference.class), any(Request.class));
  }

  @Test
  void shouldNotSendForUnsupportedCommand() {
    // given
    // no command-specific stubbing required for unsupported command

    Command command = new StringType("unsupported");

    // when
    handler.handle(command, context);

    // then
    verify(sender, never()).send(any(ChargerReference.class), any(Request.class));
  }
}
