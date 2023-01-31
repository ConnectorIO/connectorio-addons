package org.connectorio.addons.communication.watchdog.internal;

import java.util.List;
import java.util.Map;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class ThingCallbackWrapper implements ThingHandlerCallback {

  private DefaultWatchdog watchdog;
  private ThingHandlerCallback callback;
  public ThingCallbackWrapper(DefaultWatchdog watchdog, ThingHandlerCallback callback) {
    this.watchdog = watchdog;
    this.callback = callback;
  }

  @Override
  public void stateUpdated(ChannelUID channelUID, State state) {
    watchdog.mark(channelUID);
    callback.stateUpdated(channelUID, state);
  }

  @Override
  public void postCommand(ChannelUID channelUID, Command command) {
    callback.postCommand(channelUID, command);
  }

  @Override
  public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
    callback.statusUpdated(thing, thingStatus);
  }

  @Override
  public void thingUpdated(Thing thing) {
    callback.thingUpdated(thing);
  }

  @Override
  public void validateConfigurationParameters(Thing thing, Map<String, Object> configurationParameters) {
    callback.validateConfigurationParameters(thing, configurationParameters);
  }

  @Override
  public void configurationUpdated(Thing thing) {
    callback.configurationUpdated(thing);
  }

  @Override
  public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID,
      Configuration configuration) {
    callback.migrateThingType(thing, thingTypeUID, configuration);
  }

  @Override
  public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
    callback.channelTriggered(thing, channelUID, event);
  }

  @Override
  public ChannelBuilder createChannelBuilder(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
    return callback.createChannelBuilder(channelUID, channelTypeUID);
  }

  @Override
  public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
    return callback.editChannel(thing, channelUID);
  }

  @Override
  public List<ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID, ChannelGroupTypeUID channelGroupTypeUID) {
    return callback.createChannelBuilders(channelGroupUID, channelGroupTypeUID);
  }

  @Override
  public boolean isChannelLinked(ChannelUID channelUID) {
    return callback.isChannelLinked(channelUID);
  }

  @Override
  public Bridge getBridge(ThingUID bridgeUID) {
    return callback.getBridge(bridgeUID);
  }
}
