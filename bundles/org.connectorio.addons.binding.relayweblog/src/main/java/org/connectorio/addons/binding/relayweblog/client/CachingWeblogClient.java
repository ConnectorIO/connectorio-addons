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
package org.connectorio.addons.binding.relayweblog.client;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterInfo;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterReading;

/**
 * Weblog client which is capable of caching results.
 *
 * This class is intended to host very basic "time-to-live" functionality needed to group requests of same kind.
 * During nature of client use it might happen that multiple tasks will request data with very close time to each other.
 * Thanks to this implementation we are able to detect them and skip calls which are redundant.
 */
public class CachingWeblogClient implements WeblogClient {

  private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();
  private final Supplier<Long> clock;
  private final long ttl;
  private final WeblogClient delegate;

  /**
   * Creates a new caching weblog client with 60s span for cache.
   *
   * @param delegate Client to which read out requests are delegated.
   */
  public CachingWeblogClient(WeblogClient delegate) {
    this(60_000, delegate);
  }

  public CachingWeblogClient(long ttl, WeblogClient delegate) {
    this(() -> Clock.systemDefaultZone().millis(), ttl, delegate);
  }

  public CachingWeblogClient(Supplier<Long> clock, long ttl, WeblogClient delegate) {
    this.clock = clock;
    this.ttl = ttl;
    this.delegate = delegate;
  }

  @Override
  public void login(String passwordHash, SigningContext signingContext) {
    delegate.login(passwordHash, signingContext);
  }

  @Override
  public List<MeterInfo> getMeters() {
    return read("meters", delegate::getMeters);
  }

  @Override
  public List<MeterReading> getReadings(String id) {
    return read("meter#" + id, () -> delegate.getReadings(id));
  }

  protected <T> List<T> read(String key, Supplier<List<T>> call) {
    Long time = clock.get();
    if (!cache.containsKey(key) || cache.get(key).isExpired(time)) {
      cache.put(key, new CachedResult<>(time + ttl, call.get()));
    }
    return cache.get(key).getResults();
  }

  static class CachedResult<T> {
    private final long expireTime;
    private final List<T> results;

    CachedResult(long expireTime, List<T> results) {
      this.expireTime = expireTime;
      this.results = results;
    }

    boolean isExpired(Long time) {
      return expireTime <= time;
    }

    List<T> getResults() {
      return results;
    }
  }

}
