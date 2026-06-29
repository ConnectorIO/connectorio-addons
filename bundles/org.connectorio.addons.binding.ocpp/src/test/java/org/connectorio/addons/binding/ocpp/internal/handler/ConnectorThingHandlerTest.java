package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import tec.uom.se.unit.Units;

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
        // Stops are matched against the connector's own running transaction, so own one first.
        StartTransactionRequest start = new StartTransactionRequest(1, "testTag", 0, ZonedDateTime.now());
        int txId = handler.handleStartTransaction(start).getTransactionId();

        StopTransactionRequest request = new StopTransactionRequest(0, ZonedDateTime.now(), txId);
        request.setIdTag("testTag");
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
        verify(callback).stateUpdated(new ChannelUID("ocpp:connector:1:powerActiveImport"), new QuantityType<>(0, Units.WATT));
        verify(callback).stateUpdated(new ChannelUID("ocpp:connector:1:currentImport"), new QuantityType<>(0, Units.AMPERE));
        verify(callback).stateUpdated(new ChannelUID("ocpp:connector:1:currentImportL1"), new QuantityType<>(0, Units.AMPERE));
        verify(callback).stateUpdated(new ChannelUID("ocpp:connector:1:currentImportL2"), new QuantityType<>(0, Units.AMPERE));
        verify(callback).stateUpdated(new ChannelUID("ocpp:connector:1:currentImportL3"), new QuantityType<>(0, Units.AMPERE));
        verify(callback).stateUpdated(new ChannelUID("ocpp:connector:1:currentOffered"), new QuantityType<>(0, Units.AMPERE));
    }
}
