package org.connectorio.addons.binding.plc4x.canopen.internal.discovery;

import java.util.function.Consumer;
import org.connectorio.addons.binding.plc4x.canopen.internal.CANopenBindingConstants;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;

public class FallbackDiscoveryResultCallback implements DiscoveryCallback {

  private final Consumer<DiscoveryResult> callback;
  private final ThingUID bridgeUID;

  public FallbackDiscoveryResultCallback(Consumer<DiscoveryResult> callback, ThingUID bridgeUID) {
    this.callback = callback;
    this.bridgeUID = bridgeUID;
  }

  @Override
  public void thingAvailable(int node, DiscoveryResult result) {
    if (result != null) {
      callback.accept(result);
      return;
    }

    // Discovery participants did not bring any information about discovery result, meaning that we have pretty
    // much a generic CANopen node which can be read via PDO/SDO requests. This is a fallback to create a generic thing.
    DiscoveryResult genericResult = DiscoveryResultBuilder
      .create(new ThingUID(CANopenBindingConstants.GENERIC_BRIDGE_THING_TYPE, bridgeUID, "" + node))
      .withLabel("Generic CANopen node " + node)
      .withRepresentationProperty("nodeId")
      .withBridge(bridgeUID)
      .withProperty("nodeId", node)
      .build();
    callback.accept(genericResult);
  }
}
