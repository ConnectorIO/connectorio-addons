package org.connectorio.addons.usb;

import java.util.stream.Stream;
import org.openhab.core.common.registry.Registry;

public interface UsbDeviceRegistry extends Registry<UsbDevice, UsbDeviceUID> {

  Stream<UsbDevice> getNetworksOfType(UsbDeviceType... type);

}
