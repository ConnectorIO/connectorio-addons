package org.connectorio.addons.communication.watchdog.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.connectorio.addons.communication.watchdog.WatchdogClock;
import org.connectorio.addons.communication.watchdog.WatchdogCondition.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

@ExtendWith(MockitoExtension.class)
class TimeoutConditionTest {

  static ChannelUID TEST_CHANNEL = new ChannelUID(new ThingUID("modbus:poller:huawei_1:huawei_1_energy"), "energy");

  @Mock
  private WatchdogClock clock;
  private long timestamp = 1_000_000_000;

  @Test
  void testConditions() {
    when(clock.getTimestamp()).thenAnswer((inv) -> timestamp);
    TimeoutCondition condition = new TimeoutCondition(clock, TEST_CHANNEL, 2000);

    assertThat(condition.evaluate())
      .isEqualTo(State.INITIALIZED);

    tick(2000);
    assertThat(condition.evaluate())
      .isEqualTo(State.FAILED);

    condition.mark();
    tick(1999);
    assertThat(condition.evaluate())
      .isEqualTo(State.OK);

    condition.mark();
    tick(2001);
    assertThat(condition.evaluate())
      .isEqualTo(State.FAILED);
  }

  void tick(long time) {
    this.timestamp += time;
  }

}