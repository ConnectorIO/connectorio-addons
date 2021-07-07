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
package org.connectorio.addons.io.transport.serial.persistent.internal;

import java.net.URI;
import java.util.Objects;
import java.util.StringTokenizer;
import org.connectorio.addons.io.transport.serial.persistent.SerialPortDelegate;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;

public class PersistentPortIdentifier implements SerialPortIdentifier {

  public static final String PERSISTENT = "Persistent";

  private final SerialPortDelegate delegate;
  private final UsbIdentifier usbIdentifier;
  private final int productId;
  private final int vendorId;
  private final String product;
  private final String manufacturer;
  private final String interfaceDescription;
  private final int interfaceNumber;
  private final String serialNumber;
  private final String serialPort;

  private SerialPortIdentifier desiredPort;

  public PersistentPortIdentifier(SerialPortDelegate delegate, UsbIdentifier usbIdentifier, int productId, int vendorId, String product,
    String manufacturer, String interfaceDescription,
    int interfaceNumber, String serialNumber, String serialPort) {
    this.delegate = delegate;
    this.usbIdentifier = usbIdentifier;

    this.productId = productId;
    this.vendorId = vendorId;
    this.product = product;
    this.manufacturer = manufacturer;
    this.interfaceDescription = interfaceDescription;
    this.interfaceNumber = interfaceNumber;
    this.serialNumber = serialNumber;
    this.serialPort = serialPort;
  }

  public static boolean isPersistent(URI portName) {
    String fragment = portName.getFragment();
    return !fragment.isEmpty() && fragment.startsWith(PERSISTENT);
  }

  public static PersistentPortIdentifier fromUri(SerialPortDelegate delegate, URI portName) {
    String fragmentUri = portName.getFragment();
    int beginIndex = fragmentUri.indexOf(",");
    String usbIdentifier = fragmentUri.substring(beginIndex, fragmentUri.indexOf(":") - beginIndex);
    UsbIdentifier identifier = new UsbIdentifier(usbIdentifier);
    return new PersistentPortIdentifier(delegate, identifier, 0, 0, null, null, null, 0, null, null);
  }

  @Override
  public String getName() {
    return "#" + PERSISTENT + "," + usbIdentifier.getIdentifier() + ":" + serialPort;
  }

  public String toString() {
    return getName() + "[ProductId=0x" + Integer.toHexString(productId)
      + ",VendorId=0x" + Integer.toHexString(vendorId)
      + ",Product=" + product
      + ",Manufacturer=" + manufacturer
      + ",InterfaceDescription=" + interfaceDescription
      + ",InterfaceNumber=" + interfaceNumber + "]";
  }

  @Override
  public SerialPort open(String owner, int timeout) throws PortInUseException {
    desiredPort = delegate.lookup(this);
    if (desiredPort == null) {
      throw new UnsatisfiedLinkError("Serial port of " + this + " not found or can not be open");
    }

    return desiredPort.open(owner, timeout);
  }

  public int getProductId() {
    return productId;
  }

  public int getVendorId() {
    return vendorId;
  }

  public String getProduct() {
    return product;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public String getInterfaceDescription() {
    return interfaceDescription;
  }

  public int getInterfaceNumber() {
    return interfaceNumber;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public String getSerialPort() {
    return serialPort;
  }

  @Override
  public boolean isCurrentlyOwned() {
    return desiredPort != null && desiredPort.isCurrentlyOwned();
  }

  @Override
  public String getCurrentOwner() {
    return desiredPort == null ? null : desiredPort.getCurrentOwner();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof PersistentPortIdentifier)) {
      return false;
    }

    PersistentPortIdentifier that = (PersistentPortIdentifier) o;
    return Objects.equals(this.usbIdentifier, that.usbIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(usbIdentifier);
  }

  static PersistentPortIdentifier create(String text) {
    if (!text.startsWith(PERSISTENT)) {
      return null;
    }

    String data = text.substring(PERSISTENT.length() + 1);
    StringTokenizer tokenizer = new StringTokenizer(data, "=,]");
    while (tokenizer.hasMoreTokens()) {

    }

    return null;
  }

  public UsbIdentifier getUsbIdentifier() {
    return usbIdentifier;
  }

}
