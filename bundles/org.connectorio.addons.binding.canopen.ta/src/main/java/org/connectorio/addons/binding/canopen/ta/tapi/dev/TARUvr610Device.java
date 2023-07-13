package org.connectorio.addons.binding.canopen.ta.tapi.dev;

import org.connectorio.addons.binding.canopen.api.CoNode;

public class TARUvr610Device extends TADevice {

  public TARUvr610Device(CoNode node, int clientId, boolean identifyOnly) {
    super(node, clientId, identifyOnly, 0x80, 6, 10);
  }
}
