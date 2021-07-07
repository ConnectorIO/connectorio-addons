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

import java.util.Objects;
import org.openhab.core.config.discovery.usbserial.UsbSerialDeviceInformation;

public class UsbIdentifier {

  private final String identifier;

  public UsbIdentifier(UsbSerialDeviceInformation deviceInformation) {
    this(Integer.toHexString(
      Objects.hash(deviceInformation.getProductId(), deviceInformation.getVendorId(), deviceInformation.getProduct(),
        deviceInformation.getManufacturer(), deviceInformation.getInterfaceDescription(),
        deviceInformation.getInterfaceNumber(), deviceInformation.getSerialNumber())
    ));
  }

  public UsbIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UsbIdentifier)) {
      return false;
    }
    UsbIdentifier that = (UsbIdentifier) o;
    return Objects.equals(identifier, that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }

  public String toString() {
    return "UsbIdentifier[" + getIdentifier() + "]";
  }

}
