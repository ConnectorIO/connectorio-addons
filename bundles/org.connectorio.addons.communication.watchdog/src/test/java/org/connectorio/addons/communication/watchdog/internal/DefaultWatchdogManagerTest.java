/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.communication.watchdog.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Semaphore;
import org.connectorio.addons.communication.watchdog.Watchdog;
import org.connectorio.addons.communication.watchdog.WatchdogBuilder;
import org.connectorio.addons.communication.watchdog.WatchdogClock;
import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.connectorio.addons.communication.watchdog.WatchdogListener;
import org.connectorio.addons.link.LinkManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

@ExtendWith(MockitoExtension.class)
class DefaultWatchdogManagerTest {

  static ChannelUID TEST_CHANNEL = new ChannelUID(new ThingUID("modbus:poller:huawei_1:huawei_1_active_power"), "power");

  @Mock
  WatchdogClock clock;

  @Mock
  LinkManager linkManager;

  @Mock
  private Thing thing;
  @Mock
  private ThingHandlerCallback callback;
  @Mock
  private WatchdogListener listener;

  long timestamp = 0;

  @Test
  void verifyUnlinkedChannel() throws InterruptedException {
    when(linkManager.isLinked(TEST_CHANNEL)).thenReturn(false);

    DefaultWatchdogManager manager = new DefaultWatchdogManager(clock, linkManager);
    WatchdogBuilder builder = manager.builder(thing);

    Watchdog watchdog = builder.withChannel(TEST_CHANNEL, 10)
      .build(callback, listener);

    manager.check(watchdog, listener);
    Mockito.verifyNoInteractions(listener);
  }

  @Test
  void verify() throws InterruptedException {
    when(clock.getTimestamp()).thenAnswer((invocation) -> timestamp);
    when(linkManager.isLinked(TEST_CHANNEL)).thenReturn(true);

    DefaultWatchdogManager manager = new DefaultWatchdogManager(clock, linkManager);
    WatchdogBuilder builder = manager.builder(thing);
    Semaphore semaphore = new Semaphore(0);

    Watchdog watchdog = builder.withChannel(TEST_CHANNEL, 1000)
      .withChannel(TEST_CHANNEL, new BlockingConditions(TEST_CHANNEL, semaphore, 1000))
      .build(callback, listener);

    manager.check(watchdog, listener);
    Mockito.verify(listener).initialized(any());

    tick(1001);
    semaphore.acquire();
    manager.check(watchdog, listener);
    Mockito.verify(listener).timeout(any());

    tick(20);
    semaphore.acquire();
    manager.check(watchdog, listener);
    Mockito.verify(listener, never()).timeout(any());

    tick(9);
    semaphore.acquire();
    watchdog.mark(TEST_CHANNEL);
    tick(1);
    semaphore.acquire();
    manager.check(watchdog, listener);
    Mockito.verify(listener).recovery(any());

    tick(1);
    semaphore.acquire();
    manager.check(watchdog, listener);
    Mockito.verify(listener).timeout(any());
    verifyNoInteractions(listener);
  }

  void tick(long time) {
    timestamp = time;
  }

  static class BlockingConditions implements WatchdogCondition {

    private final ChannelUID channel;
    private final Semaphore semaphore;
    private final long interval;

    private State state;

    BlockingConditions(ChannelUID channel, Semaphore semaphore, long interval) {
      this.channel = channel;
      this.semaphore = semaphore;
      this.interval = interval;
    }

    @Override
    public State evaluate() {
      if (state == null) {
        state = State.INITIALIZED;
      } else {
        state = State.OK;
      }
      return state;
    }

    @Override
    public State getState() {
      return state;
    }

    @Override
    public long getInterval() {
      return interval;
    }

    @Override
    public void mark() {
      semaphore.release();
    }

    @Override
    public ChannelUID getChannel() {
      return channel;
    }
  }

}