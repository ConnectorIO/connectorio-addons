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
    when(context.getConnectorId()).thenReturn(2);
    when(sender.send(any(ChargerReference.class), any(Request.class)))
      .thenReturn(CompletableFuture.completedFuture(null));

    // when
    handler.handle(OnOffType.ON, context);

    // then
    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sender).send(any(ChargerReference.class), requestCaptor.capture());

    assertThat(requestCaptor.getValue())
      .isInstanceOf(RemoteStartTransactionRequest.class);
    RemoteStartTransactionRequest sent = (RemoteStartTransactionRequest) requestCaptor.getValue();
    assertThat(sent.getIdTag()).isEqualTo("openhab");
    // The remote start must target the connector — required for multi-connector chargers.
    assertThat(sent.getConnectorId()).isEqualTo(2);
  }

  @Test
  void shouldOmitConnectorIdWhenNotPositive() {
    // given — a connectorId of 0 (or null) is invalid for RemoteStart; it must be omitted.
    when(context.getOcppSender()).thenReturn(sender);
    when(context.getChargerSerialNumber()).thenReturn("charger-serial");
    when(context.getRemoteStartTag()).thenReturn("openhab");
    when(context.getConnectorId()).thenReturn(0);
    when(sender.send(any(ChargerReference.class), any(Request.class)))
      .thenReturn(CompletableFuture.completedFuture(null));

    // when
    handler.handle(OnOffType.ON, context);

    // then
    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sender).send(any(ChargerReference.class), requestCaptor.capture());

    RemoteStartTransactionRequest sent = (RemoteStartTransactionRequest) requestCaptor.getValue();
    assertThat(sent.getConnectorId()).isNull();
    assertThat(sent.validate()).isTrue();
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
}
