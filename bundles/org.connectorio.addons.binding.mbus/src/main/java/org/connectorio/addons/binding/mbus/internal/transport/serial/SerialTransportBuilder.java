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
package org.connectorio.addons.binding.mbus.internal.transport.serial;

import java.io.IOException;
import org.connectorio.addons.io.transport.serial.config.SerialPortConfig;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openmuc.jmbus.MBusAdapterConnectionBuilder;
import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.transportlayer.TransportLayer;

/**
 * Serial transport builder which does not utilize jrxtx which is fork of ancient rxtx.
 *
 * Given that runtime may ship different serial port provider carrying over jrxtx brings a risk of
 * incompatible native libraries as well as other low level operating system troubles.
 *
 * Below builder is intended to be used only with binding, however it can be also used elsewhere.
 */
public class SerialTransportBuilder {

  private final SerialPortManager serialPortProvider;
  private final MBusAdapterConnectionBuilder builder;
  private String serialPortName;
  private SerialPortConfig serialPortConfig;
  private int timeout;


  public SerialTransportBuilder(SerialPortManager serialPortProvider, String serialPortName, MBusAdapterConnectionBuilder builder) {
    this.builder = builder;
    this.serialPortProvider = serialPortProvider;
    this.serialPortName = serialPortName;
  }

  public SerialTransportBuilder setTimeout(int timeout) {
    this.timeout = timeout;
    return self();
  }

  public SerialTransportBuilder setSerialPortConfig(SerialPortConfig serialPortConfig) {
    this.serialPortConfig = serialPortConfig;
    return self();
  }

  public MBusConnection build() throws Exception {
    return builder.build(buildTransportLayer());
  }

  private SerialTransportBuilder self() {
    return this;
  }

  protected TransportLayer buildTransportLayer() throws IOException {
    SerialPortIdentifier portIdentifier = serialPortProvider.getIdentifier(serialPortName);
    if (portIdentifier == null) {
      throw new IOException("Port " + serialPortName + " not found");
    }
    SerialTransportLayer transportLayer = new SerialTransportLayer(portIdentifier, serialPortProvider, serialPortConfig);
    transportLayer.setTimeout(timeout);
    return transportLayer;
  }

}