package org.connectorio.addons.binding.canopen.handler;

import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.spi.values.PlcBYTE;
import org.apache.plc4x.java.spi.values.PlcStruct;
import org.apache.plc4x.java.spi.values.PlcUSINT;
import org.connectorio.addons.binding.canopen.api.CoConnection;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.api.CoSubscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

@ExtendWith(MockitoExtension.class)
class HeartbeatMonitorTest {

  @Mock
  HeartbeatCallback callback;
  @Mock
  CoConnection connection;
  @Mock
  CoSubscription subscription;
  @Mock
  CoNode node;
  @Mock
  Clock clock;

  @Test
  void checkTimeoutHandling() {
    long now = 1000000L;
    long timeoutMs = 120_000L;

    when(node.getNodeId()).thenReturn(32);
    when(connection.heartbeat(eq(32), any(Consumer.class))).thenReturn(CompletableFuture.completedFuture(subscription));
    when(clock.millis()).thenReturn(now);

    HeartbeatMonitor monitor = new HeartbeatMonitor(clock, connection, node, callback, now, timeoutMs);
    monitor.run();
    verifyNoInteractions(callback);

    when(clock.millis()).thenReturn(now + timeoutMs);
    monitor.run();
    verify(callback).updateStatus(eq(ThingStatus.OFFLINE), eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString());
    reset(callback);

    // heartbeat comes into play
    Map<String, PlcValue> fields = new HashMap<>();
    fields.put("node", new PlcUSINT(32));
    fields.put("state",new PlcBYTE(0x05));
    PlcStruct heartbeat = new PlcStruct(fields);
    when(clock.millis()).thenReturn(now + (2 * timeoutMs));
    monitor.accept(heartbeat);
    // confirm node comes back to online
    verify(callback).updateStatus(eq(ThingStatus.ONLINE));
    reset(callback);

    // check if maximum timeout - 1 does not generate an offline state
    when(clock.millis()).thenReturn(now + (3 * timeoutMs) - 1);
    monitor.run();
    verifyNoInteractions(callback);

  }


}