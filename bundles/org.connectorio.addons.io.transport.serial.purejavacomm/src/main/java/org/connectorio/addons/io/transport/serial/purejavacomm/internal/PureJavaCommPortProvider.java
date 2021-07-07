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
package org.connectorio.addons.io.transport.serial.purejavacomm.internal;

import java.net.URI;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openhab.core.io.transport.serial.ProtocolType;
import org.openhab.core.io.transport.serial.ProtocolType.PathType;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;

/**
 * A pure java serial port provider.
 *
 * Underlying implementation is based upon purejavacomm library and JNA.
 */
@Component(service = SerialPortProvider.class)
public class PureJavaCommPortProvider implements SerialPortProvider {

  private final Logger logger = LoggerFactory.getLogger(PureJavaCommPortProvider.class);

  @Override
  public SerialPortIdentifier getPortIdentifier(URI port) {
    CommPortIdentifier ident = null;
    try {
      ident = CommPortIdentifier.getPortIdentifier(port.getPath());
    } catch (NoSuchPortException e) {
      logger.debug("No SerialPortIdentifier found for: {}", port.getPath());
      return null;
    }
    return new PureJavaCommSerialPortIdentifier(ident);
  }

  @Override
  public Stream<ProtocolType> getAcceptedProtocols() {
    return Stream.of(new ProtocolType(PathType.LOCAL, "purejavacomm"));
  }

  @Override
  public Stream<SerialPortIdentifier> getSerialPortIdentifiers() {
    @SuppressWarnings("unchecked")
    final Enumeration<CommPortIdentifier> ids = CommPortIdentifier.getPortIdentifiers();
    return StreamSupport.stream(new SplitIteratorForEnumeration<>(ids), false)
        .filter(id -> id.getPortType() == CommPortIdentifier.PORT_SERIAL)
        .map(PureJavaCommSerialPortIdentifier::new);
  }

  private static class SplitIteratorForEnumeration<T> extends Spliterators.AbstractSpliterator<T> {
    private final Enumeration<T> e;

    public SplitIteratorForEnumeration(final Enumeration<T> e) {
      super(Long.MAX_VALUE, Spliterator.ORDERED);
      this.e = e;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      if (e.hasMoreElements()) {
        action.accept(e.nextElement());
        return true;
      }
      return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
      while (e.hasMoreElements()) {
        action.accept(e.nextElement());
      }
    }
  }

}
