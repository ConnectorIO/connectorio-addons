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
package org.connectorio.addons.network.core.internal.iface;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.NetworkType;
import org.connectorio.addons.network.NetworkUID;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.common.registry.ProviderChangeListener;

@ExtendWith(MockitoExtension.class)
class NetworkInterfaceNetworkProviderTest {

  @Mock
  private ProviderChangeListener<Network> listener;

  @Mock
  private NetworkInterface networkInterface1;
  @Mock
  private NetworkInterface networkInterface2;

  @Test
  public void testAdd() {
    NetworkInterfaceNetworkProvider provider = new NetworkInterfaceNetworkProvider();
    provider.addProviderChangeListener(listener);

    Network n1 = new TestNetwork("a");
    Network n2 = new TestNetwork("b");
    when(networkInterface1.getNetworks()).thenReturn(Arrays.asList(n1, n2));

    provider.added(networkInterface1);

    verify(listener).added(provider, n1);
    verify(listener).added(provider, n2);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void testRemove() {
    NetworkInterfaceNetworkProvider provider = new NetworkInterfaceNetworkProvider();
    provider.addProviderChangeListener(listener);

    Network n1 = new TestNetwork("a");
    Network n2 = new TestNetwork("b");
    when(networkInterface1.getNetworks()).thenReturn(Arrays.asList(n1, n2));

    provider.added(networkInterface1);
    provider.removed(networkInterface1);

    verify(listener).added(provider, n1);
    verify(listener).added(provider, n2);
    verify(listener).removed(provider, n1);
    verify(listener).removed(provider, n2);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void testDiffNoOldNetworks() {
    NetworkInterfaceNetworkProvider provider = new NetworkInterfaceNetworkProvider();
    provider.addProviderChangeListener(listener);

    Network n1 = new TestNetwork("a");
    Network n2 = new TestNetwork("b");
    when(networkInterface1.getNetworks()).thenReturn(Collections.emptyList());
    when(networkInterface2.getNetworks()).thenReturn(Arrays.asList(n1, n2));

    provider.added(networkInterface1);
    provider.updated(networkInterface1, networkInterface2);

    verify(listener).added(provider, n1);
    verify(listener).added(provider, n2);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void testDiffNoNewNetworks() {
    NetworkInterfaceNetworkProvider provider = new NetworkInterfaceNetworkProvider();
    provider.addProviderChangeListener(listener);

    Network n1 = new TestNetwork("a");
    Network n2 = new TestNetwork("b");
    when(networkInterface1.getNetworks()).thenReturn(Arrays.asList(n1, n2));
    when(networkInterface2.getNetworks()).thenReturn(Collections.emptyList());

    provider.added(networkInterface1);
    provider.updated(networkInterface1, networkInterface2);

    verify(listener).added(provider, n1);
    verify(listener).added(provider, n2);
    verify(listener).removed(provider, n1);
    verify(listener).removed(provider, n2);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void testDiffChangedNetwork() {
    NetworkInterfaceNetworkProvider provider = new NetworkInterfaceNetworkProvider();
    provider.addProviderChangeListener(listener);

    Network n1 = new TestNetwork("a");
    Network n2 = new TestNetwork("b");
    when(networkInterface1.getNetworks()).thenReturn(Arrays.asList(n1));
    when(networkInterface2.getNetworks()).thenReturn(Arrays.asList(n2));

    provider.added(networkInterface1);
    provider.updated(networkInterface1, networkInterface2);

    verify(listener).added(provider, n1);
    verify(listener).removed(provider, n1);
    verify(listener).added(provider, n2);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void testDiffChangedNetworkUpdateCall() {
    NetworkInterfaceNetworkProvider provider = new NetworkInterfaceNetworkProvider();
    provider.addProviderChangeListener(listener);

    Network n1a = new TestNetwork("a");
    Network n1b = new TestNetwork("a", 1);
    when(networkInterface1.getNetworks()).thenReturn(Arrays.asList(n1a));
    when(networkInterface2.getNetworks()).thenReturn(Arrays.asList(n1b));

    provider.added(networkInterface1);
    provider.updated(networkInterface1, networkInterface2);

    verify(listener).added(provider, n1a);
    verify(listener).updated(provider, n1a, n1b);
    verifyNoMoreInteractions(listener);
  }

  static class TestNetwork implements Network {

    private static final NetworkType TEST = new NetworkType() {
      @Override
      public String getType() {
        return "test";
      }
    };
    private final NetworkUID uid;
    private final int index;

    TestNetwork(NetworkUID uid, int index) {
      this.uid = uid;
      this.index = index;
    }

    TestNetwork(String id) {
      this(new NetworkUID(TEST.getType(), id), 0);
    }

    TestNetwork(String id, int index) {
      this(new NetworkUID(TEST.getType(), id), index);
    }

    @Override
    public NetworkType getType() {
      return TEST;
    }

    @Override
    public NetworkUID getUID() {
      return uid;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TestNetwork)) {
        return false;
      }
      TestNetwork that = (TestNetwork) o;
      return Objects.equals(uid, that.uid) && Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
      return Objects.hash(uid, index);
    }
  }

}