package org.connectorio.binding.bacnet.internal.discovery;

import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.ANALOG_INPUT_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.ANALOG_OUTPUT_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.ANALOG_VALUE_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.BINARY_INPUT_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.BINARY_OUTPUT_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.BINARY_VALUE_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.IP_DEVICE_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.MULTISTATE_INPUT_THING_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.MULTISTATE_OUTPUT_TYPE;
import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.MULTISTATE_VALUE_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.Property;
import org.connectorio.binding.bacnet.internal.handler.property.BACnetDeviceBridgeHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BACnetPropertyDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {

  private final Logger logger = LoggerFactory.getLogger(BACnetPropertyDiscoveryService.class);

  private BACnetDeviceBridgeHandler<?, ?> handler;

  public BACnetPropertyDiscoveryService() throws IllegalArgumentException {
    super(Collections.singleton(IP_DEVICE_THING_TYPE), 300);
  }

  @Override
  protected void startBackgroundDiscovery() {
    startScan();
  }

  @Override
  protected void startScan() {
    handler.getClient().map(client -> {
      BacNetClient bacNetClient;
      try {
        bacNetClient = client.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return null;
      }

      return bacNetClient.getDeviceProperties(handler.getDevice());
    })
    .ifPresent(properties -> properties.stream()
      .map(property -> toDiscoveryResult(handler, property))
      .filter(Objects::nonNull)
      .forEach(this::thingDiscovered));
  }

  private DiscoveryResult toDiscoveryResult(BACnetDeviceBridgeHandler<?, ?> bridge, Property property) {
    DiscoveryResultBuilder builder;
    String id = "" + property.getId();

    switch (property.getType()) {
      case ANALOG_INPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(ANALOG_INPUT_THING_TYPE, id));
        break;
      case ANALOG_OUTPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(ANALOG_OUTPUT_THING_TYPE, id));
        break;
      case ANALOG_VALUE:
        builder = DiscoveryResultBuilder.create(new ThingUID(ANALOG_VALUE_THING_TYPE, id));
        break;
      case BINARY_INPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(BINARY_INPUT_THING_TYPE, id));
        break;
      case BINARY_OUTPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(BINARY_OUTPUT_THING_TYPE, id));
        break;
      case BINARY_VALUE:
        builder = DiscoveryResultBuilder.create(new ThingUID(BINARY_VALUE_THING_TYPE, id));
        break;
      case MULTISTATE_INPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(MULTISTATE_INPUT_THING_TYPE, id));
        break;
      case MULTISTATE_OUTPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(MULTISTATE_OUTPUT_TYPE, id));
        break;
      case MULTISTATE_VALUE:
        builder = DiscoveryResultBuilder.create(new ThingUID(MULTISTATE_VALUE_TYPE, id));
        break;
      default:
        logger.info("Unsupported object type " + property.getType());
        return null;

    }

    return builder.withLabel(property.getName())
      .withProperty("description", property.getDescription())
      .withBridge(bridge.getThing().getUID())
      .withProperty("instance", property.getId())
      .withRepresentationProperty("instance")
      .build();
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof BACnetDeviceBridgeHandler) {
      this.handler = (BACnetDeviceBridgeHandler<?, ?>) handler;
    }
  }

  @Override
  public ThingHandler getThingHandler() {
    return this.handler;
  }

  @Override
  public void activate() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
    super.activate(properties);
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

}
