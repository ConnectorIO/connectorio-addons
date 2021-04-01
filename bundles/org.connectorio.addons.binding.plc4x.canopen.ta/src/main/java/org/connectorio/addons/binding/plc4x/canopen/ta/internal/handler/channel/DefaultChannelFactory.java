package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider.TAChannelTypeProvider;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelFactory implements ChannelFactory {

  private final Logger logger = LoggerFactory.getLogger(DefaultChannelFactory.class);

  @Override
  public <T extends Value<?>> CompletableFuture<List<Channel>> create(ThingUID thing, TACanInputOutputObject<T> object) {
    logger.debug("Creating new channel(s) in {} from received object {}", thing, object);
    if (object instanceof TAAnalogOutput) {
      TAAnalogOutput analogOutput = (TAAnalogOutput) object;

      AnalogUnit unit = AnalogUnit.valueOf(analogOutput.getUnit());
      if (unit == null) {
        logger.info("Ignoring I/O object {} object. Could not find matching channel and thing for unit {}", object, analogOutput.getUnit());
        return CompletableFuture.completedFuture(Collections.emptyList());
      }

      return analogOutput.getName().handle((name, error) -> {
        if (error != null) {
          logger.debug("Failed to read analog label", error);
          name = "Analog object #" + analogOutput.getIndex();
        }

        return createChannels(thing, object, unit, name);
      });
    }
    if (object instanceof TADigitalOutput) {
      TADigitalOutput digitalOutput = (TADigitalOutput) object;

      DigitalUnit unit = DigitalUnit.valueOf(digitalOutput.getUnit());
      if (unit == null) {
        logger.info("Ignoring I/O object {} object. Could not find matching channel and thing for unit {}", object, digitalOutput.getUnit());
        return CompletableFuture.completedFuture(Collections.emptyList());
      }

      return digitalOutput.getName().handle((name, error) -> {
        if (error != null) {
          logger.debug("Failed to read digital label", error);
          name = "Digital object #" + digitalOutput.getIndex();
        }

        return createChannels(thing, object, unit, name);
      });
    }

    logger.debug("No matching channel found for thing {} and object {}", thing, object);
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  private List<Channel> createChannels(ThingUID thing, TACanInputOutputObject<?> object, TAUnit unit, String name) {
    return TAChannelTypeProvider.forObject(thing, object, unit, name);
    /*
    List<Channel> channels = new ArrayList<>();
    for (ChannelTypeUID channelType : TAChannelTypeProvider.forObject(object, unit, name)) {
      Map<String, Object> configuration = new HashMap<>();
      configuration.put("readObjectIndex", object.getIndex());
      configuration.put("unit", unit.name());

      ChannelUID uid = new ChannelUID(thing,  channelType.getId() + "#" + object.getIndex());
      ChannelBuilder channelBuilder = ChannelBuilder.create(uid)
        .withLabel(name)
        .withType(channelType)
        .withConfiguration(new Configuration(configuration));

      channels.add(channelBuilder.build());
    }
    return channels;
    //*/
  }

}
