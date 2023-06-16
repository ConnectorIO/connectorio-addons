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
package org.connectorio.addons.binding.fatek.internal.transport.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.connectorio.addons.binding.fatek.internal.transport.ReflectiveCall;
import org.connectorio.addons.config.ConfigMapperFactory;
import org.connectorio.addons.io.transport.serial.config.SerialPortConfig;
import org.connectorio.addons.io.transport.serial.SerialPortConfigurator;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.osgi.service.component.annotations.Component;
import org.simplify4u.jfatek.io.FatekConfig;
import org.simplify4u.jfatek.io.FatekConnection;
import org.simplify4u.jfatek.io.FatekConnectionFactory;

@Component
public class FatekSerialConnectionFactory implements FatekConnectionFactory {

  private final SerialPortManager serialPortManager;
  private final ConfigMapperFactory configMapperFactory;

  public FatekSerialConnectionFactory(SerialPortManager serialPortManager, ConfigMapperFactory configMapperFactory) {
    this.serialPortManager = serialPortManager;
    this.configMapperFactory = configMapperFactory;
  }

  @Override
  public FatekConnection getConnection(FatekConfig fatekConfig) throws IOException {
    SerialPortIdentifier identifier = serialPortManager.getIdentifier(fatekConfig.getFullName());
    Map<String, Object> params = ReflectiveCall.field(FatekConfig.class, fatekConfig, "params");
    SerialPortConfig serialPortConfig = configMapperFactory.createMapper(SerialPortConfig.class).map(params);
    int timeout = fatekConfig.getTimeout();
    if (identifier != null) {
      try {
        SerialPort port = identifier.open("fatek-binding", timeout);
        if (serialPortConfig != null) {
          new SerialPortConfigurator(port).configure(serialPortConfig);
        }
        return new SerialConnection(fatekConfig, port);
      } catch (PortInUseException | UnsupportedCommOperationException e) {
        throw new IOException("Could not open port", e);
      }
    }
    throw new IOException("Port " + fatekConfig.getFullName() + " not found");
  }

  @Override
  public String getSchema() {
    return "serial";
  }

  static class SerialConnection extends FatekConnection {

    private final SerialPort port;
    private boolean closed;

    public SerialConnection(FatekConfig config, SerialPort port) {
      super(config);
      this.port = port;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
      return port.getInputStream();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
      return port.getOutputStream();
    }

    @Override
    protected void closeConnection() throws IOException {
      port.close();
      this.closed = true;
    }

    @Override
    public boolean isConnected() {
      return !closed;
    }
  }

}
