package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import tech.units.indriya.unit.Units;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectorThingHandlerTest {

    private ConnectorThingHandler handler;
    private ThingHandlerCallback callback;
    private Thing thing;

    @BeforeEach
    void setUp() {
        thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("ocpp:connector:1"));
        handler = new ConnectorThingHandler(thing);
        callback = mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
    }

    @Test
    void testResetOnStopTransaction() {
        StopTransactionRequest request = mock(StopTransactionRequest.class);
        when(request.getIdTag()).thenReturn("testTag");
        when(request.getTransactionId()).thenReturn(1);
        when(request.getTimestamp()).thenReturn(java.time.ZonedDateTime.now());
        when(request.getMeterStop()).thenReturn(0);
        // Simulate transactionId state
        java.lang.reflect.Field txIdField;
        try {
            txIdField = ConnectorThingHandler.class.getDeclaredField("transactionId");
            txIdField.setAccessible(true);
            java.util.concurrent.atomic.AtomicInteger txId = new java.util.concurrent.atomic.AtomicInteger(0);
            txId.set(2); // so txId.get() == 2, request.getTransactionId() == 1
            txIdField.set(handler, txId);
        } catch (Exception e) {
            fail(e);
        }
        handler.handleStopTransaction(request);
        verifyResetChannels();
    }

    @Test
    void testResetOnStatusNotificationAvailable() {
        StatusNotificationRequest request = mock(StatusNotificationRequest.class);
        when(request.getStatus()).thenReturn(ChargePointStatus.Available);
        handler.handleStatusNotification(request);
        verifyResetChannels();
    }

    @Test
    void testResetOnStatusNotificationFinishing() {
        StatusNotificationRequest request = mock(StatusNotificationRequest.class);
        when(request.getStatus()).thenReturn(ChargePointStatus.Finishing);
        handler.handleStatusNotification(request);
        verifyResetChannels();
    }

    private void verifyResetChannels() {
        verify(callback).stateUpdated(argThat(uid -> uid.getId().equals("powerActiveImport")), eq(new QuantityType<>(0, Units.WATT)));
        verify(callback).stateUpdated(argThat(uid -> uid.getId().equals("currentImport")), eq(new QuantityType<>(0, Units.AMPERE)));
        verify(callback).stateUpdated(argThat(uid -> uid.getId().equals("currentImportL1")), eq(new QuantityType<>(0, Units.AMPERE)));
        verify(callback).stateUpdated(argThat(uid -> uid.getId().equals("currentImportL2")), eq(new QuantityType<>(0, Units.AMPERE)));
        verify(callback).stateUpdated(argThat(uid -> uid.getId().equals("currentImportL3")), eq(new QuantityType<>(0, Units.AMPERE)));
        verify(callback).stateUpdated(argThat(uid -> uid.getId().equals("currentOffered")), eq(new QuantityType<>(0, Units.AMPERE)));
    }
}
