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
package org.connectorio.addons.binding.wmbus.internal.discovery;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.connectorio.addons.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class ChainedDiscoveryCoordinator implements DiscoveryCoordinator {

  private final Set<DiscovererEntry> discoveryEntries = Collections.synchronizedSet(new TreeSet<>(
    Comparator.comparing(DiscovererEntry::getRanking).thenComparing(DiscovererEntry::hashCode)
  ));

  @Override
  public DiscoveryResult discover(ThingUID bridgeUID, long timeToLive, WMBusMessage message) {
    for (DiscovererEntry entry : discoveryEntries) {
      Optional<DiscoveryResult> result = entry.getParticipant().discover(bridgeUID, timeToLive, message);
      if (result.isPresent()) {
        return result.get();
      }
    }

    return null;
  }

  @Reference
  void addDiscoveryParticipant(WMBusDiscoveryParticipant participant, Map<String, Object> props) {
    int ranking = determineServiceRanking(props);
    discoveryEntries.add(new DiscovererEntry(ranking, participant));
  }

  void removeDiscoveryParticipant(WMBusDiscoveryParticipant participant, Map<String, Object> props) {
    int ranking = determineServiceRanking(props);
    discoveryEntries.remove(new DiscovererEntry(ranking, participant));
  }

  private static Integer determineServiceRanking(Map<String, Object> props) {
    return Optional.ofNullable(props.get(Constants.SERVICE_RANKING))
        .map(property -> Integer.parseInt("" + property))
        .orElse(0);
  }

  static class DiscovererEntry {
    final int ranking;
    final WMBusDiscoveryParticipant participant;

    DiscovererEntry(int ranking, WMBusDiscoveryParticipant participant) {
      this.ranking = ranking;
      this.participant = participant;
    }

    public int getRanking() {
      return ranking;
    }

    public WMBusDiscoveryParticipant getParticipant() {
      return participant;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DiscovererEntry)) {
        return false;
      }
      DiscovererEntry that = (DiscovererEntry) o;
      return getRanking() == that.getRanking() && Objects.equals(getParticipant(), that.getParticipant());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getRanking(), getParticipant());
    }
  }

}
