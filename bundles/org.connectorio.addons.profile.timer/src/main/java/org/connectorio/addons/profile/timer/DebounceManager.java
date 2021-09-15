/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.profile.timer;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * High level interface definition for debounce management.
 */
public interface DebounceManager {

  /**
   * Schedules an delayed action.
   *
   * @param action Action to be executed.
   * @param delay Delay being taken.
   * @param unit Time unit of the delay.
   * @param <T> Type of result.
   * @return Scheduled future which can be used to cancel call.
   */
  <T> ScheduledFuture<T> schedule(Callable<T> action, long delay, TimeUnit unit);

}
