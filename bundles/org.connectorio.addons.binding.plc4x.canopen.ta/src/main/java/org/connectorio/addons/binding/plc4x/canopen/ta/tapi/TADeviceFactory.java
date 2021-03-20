package org.connectorio.addons.binding.plc4x.canopen.ta.tapi;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TARsm610Device;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TAUvr16x2Device;

public class TADeviceFactory {

  private final boolean identifyOnly;

  public TADeviceFactory() {
    this(false);
  }

  public TADeviceFactory(boolean identifyOnly) {
    this.identifyOnly = identifyOnly;
  }

  public CompletableFuture<TADevice> create(CoNode node, int clientId) {
    return node.<Short>read((short) 0x23E2, (short) 0x01, CANOpenDataType.UNSIGNED8)
      .thenApply(deviceType -> {
        if (deviceType == 0x87) {
          return new TAUvr16x2Device(node, clientId, identifyOnly);
        } else if (deviceType == 0x88) {
          return new TARsm610Device(node, clientId, identifyOnly);
        } else if (deviceType == 0x8A) {
          // virtual device
          return new TARsm610Device(node, clientId, identifyOnly);
        }
        throw new IllegalArgumentException("Unsupported device " + Integer.toHexString(deviceType));
      });
  }

}
