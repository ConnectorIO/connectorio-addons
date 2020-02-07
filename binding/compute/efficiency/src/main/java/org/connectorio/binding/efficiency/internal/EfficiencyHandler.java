package org.connectorio.binding.efficiency.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.connectorio.binding.efficiency.internal.memo.StateCollector;
import org.connectorio.binding.efficiency.internal.ventilation.heatex.HeatExConfig;
import org.connectorio.binding.efficiency.internal.ventilation.heatex.HeatExState;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage.Builder;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class EfficiencyHandler extends ConfigStatusThingHandler {

  private final ItemRegistry itemRegistry;
  private final StateCollector<ItemStateChangedEvent> collector;
  private HeatExConfig config;
  private HeatExState heatEx;

  public EfficiencyHandler(Thing thing, ItemRegistry itemRegistry, StateCollector<ItemStateChangedEvent> collector) {
    super(thing);
    this.itemRegistry = itemRegistry;
    this.collector = collector;
  }

  @Override
  public void initialize() {
    this.config = getConfigAs(HeatExConfig.class);

    if (this.config != null) {
      updateStatus(ThingStatus.ONLINE);
    } else {
      updateStatus(ThingStatus.OFFLINE);
    }
  }

  @Override
  public Collection<ConfigStatusMessage> getConfigStatus() {
    HeatExConfig config = getConfigAs(HeatExConfig.class);

    List<ConfigStatusMessage> status = new ArrayList<>();
    if (config.extractTemperature == null) {
      status.add(Builder.pending("extractTemperature").build());
    } else {
      validate(status, "extractTemperature", config.extractTemperature);
    }

    if (config.supplyTemperature == null) {
      status.add(Builder.pending("supplyTemperature").build());
    } else {
      validate(status, "supplyTemperature", config.supplyTemperature);
    }

    if (config.intakeTemperature == null) {
      status.add(Builder.pending("intakeTemperature").build());
    } else {
      validate(status, "intakeTemperature", config.intakeTemperature);
    }

    return status;
  }

  private void validate(List<ConfigStatusMessage> status, String parameter, String itemName) {
    try {
      Item registryItem = itemRegistry.getItem(itemName);
      if (!registryItem.getType().startsWith(CoreItemFactory.NUMBER)) {
        status.add(ConfigStatusMessage.Builder.error(parameter).withMessageKeySuffix("wrong-type").build());
      }
    } catch (ItemNotFoundException e) {
      status.add(ConfigStatusMessage.Builder.error(parameter).withMessageKeySuffix("missing").build());
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    // things of this type are rather passive thus they can't do much ;-)

  }

  @Override
  public void channelLinked(ChannelUID channelUID) {
    super.channelLinked(channelUID);

    this.heatEx = new HeatExState(System::currentTimeMillis, getCallback(), channelUID, config);
    this.collector.addStateReceiver(heatEx);
  }

  @Override
  public void channelUnlinked(ChannelUID channelUID) {
    super.channelUnlinked(channelUID);

    this.collector.removeStateReceiver(heatEx);
    this.heatEx = null;
  }

}
