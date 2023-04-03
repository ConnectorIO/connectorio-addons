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
package org.connectorio.addons.binding.wmbus.internal.transport.serial;

import java.io.IOException;
import org.connectorio.addons.io.transport.mbus.config.SerialPortConfig.DataBits;
import org.connectorio.addons.io.transport.mbus.config.SerialPortConfig.FlowControl;
import org.connectorio.addons.io.transport.mbus.config.SerialPortConfig.Parity;
import org.connectorio.addons.io.transport.mbus.config.SerialPortConfig.StopBits;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openmuc.jmbus.transportlayer.TransportLayer;
import org.openmuc.jmbus.wireless.WMBusAdapterConnectionBuilder;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusManufacturer;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMode;

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
  private final WMBusAdapterConnectionBuilder builder;
  private String serialPortName;
  private int baudRate;
  private int dataBits;
  private int stopBits;
  private int parity;
  private int flowControl;
  private boolean rts;
  private int timeout;

  public SerialTransportBuilder(SerialPortManager serialPortProvider, WMBusManufacturer wmBusManufacturer, WMBusListener listener, String serialPortName) {
    builder = new WMBusAdapterConnectionBuilder(wmBusManufacturer, listener);

    this.serialPortProvider = serialPortProvider;
    this.serialPortName = serialPortName;
  }

  public SerialTransportBuilder setMode(WMBusMode mode) {
    builder.setMode(mode);
    return self();
  }

  public SerialTransportBuilder setBaudRate(int baudRate) {
    this.baudRate = baudRate;
    return self();
  }

  public SerialTransportBuilder setDataBits(DataBits dataBits) {
    this.dataBits = dataBits.getDataBits();
    return self();
  }

  public SerialTransportBuilder setStopBits(StopBits stopBits) {
    this.stopBits = stopBits.getStopBits();
    return self();
  }

  public SerialTransportBuilder setParity(Parity parity) {
    this.parity = parity.getParity();
    return self();
  }

  public SerialTransportBuilder setFlowControl(FlowControl flowControl) {
    this.flowControl = flowControl.getFlowControl();
    return self();
  }

  public SerialTransportBuilder setRts(boolean rts) {
    this.rts = rts;
    return self();
  }

  public SerialTransportBuilder setTimeout(int timeout) {
    this.timeout = timeout;
    return self();
  }


  public WMBusConnection build() throws IOException {
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
    SerialTransportLayer transportLayer = new SerialTransportLayer(
      portIdentifier, serialPortProvider, baudRate, dataBits, stopBits, parity,
      flowControl, rts
    );
    transportLayer.setTimeout(timeout);
    return transportLayer;
  }

}