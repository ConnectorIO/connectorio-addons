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
package org.connectorio.addons.binding.fatek.internal.discovery;

import java.util.Arrays;
import java.util.Set;
import org.connectorio.addons.binding.fatek.FatekBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.openhab.core.config.discovery.usbserial.UsbSerialDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = {DiscoveryService.class, UsbSerialDiscoveryParticipant.class})
public class SerialInterfaceDiscoveryService extends AbstractFatekDiscoveryService implements
    UsbSerialDiscoveryParticipant {

  private final static int VENDOR_ID = 0x0403;
  private final static int PRODUCT_ID = 0x6015;
  private final static int INTERFACE_NO = 0x00;
  private final static String INTERFACE_DESCRIPTION = "FT230X Basic UART";
  private final static String MANUFACTURER = "FTDI";

  @Activate
  public SerialInterfaceDiscoveryService(@Reference DiscoveryCoordinator discoveryCoordinator) {
    super(Arrays.asList(
      FatekBindingConstants.SERIAL_BRIDGE_TYPE,
      FatekBindingConstants.PLC_THING_TYPE
    ), discoveryCoordinator);
  }

  @Override
  protected void startScan() {

  }

  @Override
  public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
    return getSupportedThingTypes();
  }

  @Override
  public DiscoveryResult createResult(UsbSerialDeviceInformation deviceInformation) {
    if (isFatekSerialAdapter(deviceInformation)) {
      return DiscoveryResultBuilder.create(deviceUID(deviceInformation))
        .withLabel("Fatek serial adapter " + deviceInformation.getSerialNumber() + " " + deviceInformation.getSerialPort())
        .withProperty(Thing.PROPERTY_SERIAL_NUMBER, deviceInformation.getSerialNumber())
        .build();
    }
    return null;
  }

  // called upon removal of device
  @Override
  public ThingUID getThingUID(UsbSerialDeviceInformation deviceInformation) {
    if (isFatekSerialAdapter(deviceInformation)) {
      return deviceUID(deviceInformation);
    }
    return null;
  }

  private static ThingUID deviceUID(UsbSerialDeviceInformation deviceInformation) {
    return new ThingUID(FatekBindingConstants.SERIAL_BRIDGE_TYPE, deviceInformation.getSerialNumber());
  }

  private static boolean isFatekSerialAdapter(UsbSerialDeviceInformation deviceInformation) {
    return VENDOR_ID == deviceInformation.getVendorId()
      && PRODUCT_ID == deviceInformation.getProductId()
      && INTERFACE_NO == deviceInformation.getInterfaceNumber()
      && MANUFACTURER.equals(deviceInformation.getManufacturer())
      && INTERFACE_DESCRIPTION.equals(deviceInformation.getInterfaceDescription());
  }
}
