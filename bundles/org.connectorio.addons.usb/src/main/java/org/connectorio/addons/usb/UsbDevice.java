package org.connectorio.addons.usb;

import org.openhab.core.common.registry.Identifiable;

public interface UsbDevice extends Identifiable<UsbDeviceUID> {

  UsbDeviceType getType();

}
