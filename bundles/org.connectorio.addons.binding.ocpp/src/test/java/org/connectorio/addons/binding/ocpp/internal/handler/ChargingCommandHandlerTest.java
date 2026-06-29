package org.connectorio.addons.binding.ocpp.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.RemoteStartTransactionRequest;
import eu.chargetime.ocpp.model.core.RemoteStopTransactionRequest;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.types.OnOffType;

@ExtendWith(MockitoExtension.class)
class ChargingCommandHandlerTest {

  @Mock
  private OcppSender sender;

  @Mock
  private ConnectorCommandContext context;

  private ChargingCommandHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ChargingCommandHandler();
  }

  @Test
  void shouldSendRemoteStartTransactionForOnCommand() {
    // given
    when(context.getOcppSender()).thenReturn(sender);
    when(context.getChargerSerialNumber()).thenReturn("charger-serial");
    when(context.getRemoteStartTag()).thenReturn("openhab");
    when(sender.send(any(ChargerReference.class), any(Request.class)))
      .thenReturn(CompletableFuture.completedFuture(null));

    // when
    handler.handle(OnOffType.ON, context);

    // then
    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sender).send(any(ChargerReference.class), requestCaptor.capture());

    assertThat(requestCaptor.getValue())
      .isInstanceOf(RemoteStartTransactionRequest.class);
    assertThat(((RemoteStartTransactionRequest) requestCaptor.getValue()).getIdTag())
      .isEqualTo("openhab");
  }

  @Test
  void shouldSendRemoteStopTransactionForOffCommand() {
    // given
    when(context.getOcppSender()).thenReturn(sender);
    when(context.getChargerSerialNumber()).thenReturn("charger-serial");
    when(context.getCurrentTransactionId()).thenReturn(123);
    when(sender.send(any(ChargerReference.class), any(Request.class)))
      .thenReturn(CompletableFuture.completedFuture(null));

    // when
    handler.handle(OnOffType.OFF, context);

    // then
    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sender).send(any(ChargerReference.class), requestCaptor.capture());

    assertThat(requestCaptor.getValue())
      .isInstanceOf(RemoteStopTransactionRequest.class);
    assertThat(((RemoteStopTransactionRequest) requestCaptor.getValue()).getTransactionId())
      .isEqualTo(123);
  }

  @Test
  void shouldNotSendWhenTransactionIdIsMissing() {
    // given
    when(context.getOcppSender()).thenReturn(sender);
    when(context.getChargerSerialNumber()).thenReturn("charger-serial");
    when(context.getCurrentTransactionId()).thenReturn(null);

    // when
    handler.handle(OnOffType.OFF, context);

    // then
    verify(sender, never()).send(any(ChargerReference.class), any(Request.class));
  }

  @Test
  void shouldIgnoreUnsupportedCommandType() {
    // given
    // no command-specific stubbing required for unsupported command

    // when
    handler.handle(new org.openhab.core.library.types.StringType("unsupported"), context);

    // then
    verify(sender, never()).send(any(ChargerReference.class), any(Request.class));
  }

  @Test
  void shouldIgnoreRefreshType() {
    // charging is a write-only RemoteStart/Stop control; REFRESH must be a no-op
    handler.handle(org.openhab.core.types.RefreshType.REFRESH, context);

    verify(sender, never()).send(any(ChargerReference.class), any(Request.class));
  }
}
