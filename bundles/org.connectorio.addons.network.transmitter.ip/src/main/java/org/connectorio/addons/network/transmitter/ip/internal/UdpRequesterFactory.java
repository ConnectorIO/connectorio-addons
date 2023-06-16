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
package org.connectorio.addons.network.transmitter.ip.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.connectorio.addons.network.ip.IpNetwork;
import org.connectorio.addons.network.transmitter.ip.UdpRequesterConfiguration;
import org.connectorio.addons.transmitter.Requester;
import org.connectorio.addons.transmitter.RequesterFactory;
import org.osgi.service.component.annotations.Component;

@Component(service = RequesterFactory.class, property = {"type=udp"})
public class UdpRequesterFactory implements RequesterFactory<IpNetwork, UdpRequesterConfiguration> {

  @Override
  public <X> Requester<X> create(IpNetwork network, UdpRequesterConfiguration configuration) throws IOException {
    network.getBroadcastAddress();
    return (Requester<X>) new UdpRequester(new InetSocketAddress(network.getBroadcastAddress(), configuration.getBroadcastPort()),
      new InetSocketAddress(network.getAddress(), configuration.getReceiverPort()));
  }

}
