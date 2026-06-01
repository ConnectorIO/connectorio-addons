package org.connectorio.addons.binding.ocpp.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import org.connectorio.addons.binding.ocpp.internal.OcppRequestListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChargerConnectorAdapterTest {

  private ConnectorThingHandler connector1;
  private ChargerConnectorAdapter adapter;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    OcppRequestListener<Request> listener = mock(OcppRequestListener.class);
    connector1 = mock(ConnectorThingHandler.class);
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
}
