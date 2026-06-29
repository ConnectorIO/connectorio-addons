package org.connectorio.addons.binding.ocpp.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.AuthorizationStatus;
import eu.chargetime.ocpp.model.core.IdTagInfo;
import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import java.util.concurrent.atomic.AtomicInteger;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChargerConnectorAdapterTest {

  @Mock
  private OcppRequestListener<Request> listener;
  @Mock
  private ConnectorThingHandler connector1;
  @Mock
  private ConnectorThingHandler connector2;

  private ChargerConnectorAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ChargerConnectorAdapter(listener);
  }

  @Test
  void chargerLevelMeterValuesIsAckedNotNull() {
    // connectorId 0 (charger-level, e.g. idle clock-aligned MeterValues) has no connector Thing.
    adapter.addConnector(1, connector1);
    MeterValuesRequest request = mock(MeterValuesRequest.class);
    when(request.getConnectorId()).thenReturn(0);

    MeterValuesConfirmation conf = adapter.handleMeterValues(request);

    // must ACK (non-null) so the OCPP layer does not answer the charge point NotSupported
    assertThat(conf).isNotNull();
    verify(connector1, never()).handleMeterValues(any(MeterValuesRequest.class));
  }

  @Test
  void chargerLevelStatusNotificationIsAckedNotNull() {
    // connectorId 0 (whole-charger status) likewise has no connector Thing.
    adapter.addConnector(1, connector1);
    StatusNotificationRequest request = mock(StatusNotificationRequest.class);
    when(request.getConnectorId()).thenReturn(0);

    StatusNotificationConfirmation conf = adapter.handleStatusNotification(request);

    assertThat(conf).isNotNull();
    verify(connector1, never()).handleStatusNotification(any(StatusNotificationRequest.class));
  }

  @Test
  void connectorsOfTheSameChargerShareOneTransactionSequence() {
    // when both connectors of one charge point are registered
    adapter.addConnector(1, connector1);
    adapter.addConnector(2, connector2);

    // then they are handed the SAME sequence instance, so their generated ids never collide
    ArgumentCaptor<AtomicInteger> seq1 = ArgumentCaptor.forClass(AtomicInteger.class);
    ArgumentCaptor<AtomicInteger> seq2 = ArgumentCaptor.forClass(AtomicInteger.class);
    verify(connector1).setTransactionSequence(seq1.capture());
    verify(connector2).setTransactionSequence(seq2.capture());
    assertThat(seq1.getValue()).isSameAs(seq2.getValue());
  }

  @Test
  void stopTransactionIsRoutedToTheConnectorThatStartedThatTransaction() {
    adapter.addConnector(1, connector1);
    adapter.addConnector(2, connector2);

    // connector 1 starts transaction id 1, connector 2 starts transaction id 2
    StartTransactionRequest start1 = startOn(1);
    StartTransactionRequest start2 = startOn(2);
    when(connector1.handleStartTransaction(start1)).thenReturn(confirmation(1));
    when(connector2.handleStartTransaction(start2)).thenReturn(confirmation(2));
    adapter.handleStartTransaction(start1);
    adapter.handleStartTransaction(start2);

    // a stop for transaction id 2 must reach connector 2 only
    StopTransactionRequest stop = mockStop(2);
    adapter.handleStopTransaction(stop);

    verify(connector2).handleStopTransaction(stop);
    verify(connector1, never()).handleStopTransaction(any(StopTransactionRequest.class));
  }

  private StartTransactionRequest startOn(int connectorId) {
    StartTransactionRequest request = mock(StartTransactionRequest.class);
    when(request.getConnectorId()).thenReturn(connectorId);
    return request;
  }

  private StartTransactionConfirmation confirmation(int transactionId) {
    return new StartTransactionConfirmation(new IdTagInfo(AuthorizationStatus.Accepted), transactionId);
  }

  private StopTransactionRequest mockStop(int transactionId) {
    StopTransactionRequest request = mock(StopTransactionRequest.class);
    when(request.getTransactionId()).thenReturn(transactionId);
    return request;
  }
}
