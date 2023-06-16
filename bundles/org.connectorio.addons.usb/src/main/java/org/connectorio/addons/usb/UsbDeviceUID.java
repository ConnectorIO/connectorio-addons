package org.connectorio.addons.usb;

import org.openhab.core.common.AbstractUID;

public class UsbDeviceUID extends AbstractUID {

  public UsbDeviceUID(String... segments) {
    super(segments);
  }

  @Override
  protected int getMinimalNumberOfSegments() {
    return 2;
  }

  public String getType() {
    return getSegment(0);
  }

}
