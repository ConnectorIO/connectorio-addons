package org.connectorio.addons.communication.watchdog;

import org.openhab.core.thing.ChannelUID;

public abstract class WatchdogEvent {

  protected final ChannelUID channelUID;

  protected WatchdogEvent(ChannelUID channelUID) {
    this.channelUID = channelUID;
  }

  public ChannelUID getChannel() {
    return channelUID;
  }

  public static class WatchdogRecoveryEvent extends WatchdogEvent {
    public WatchdogRecoveryEvent(ChannelUID channelUID) {
      super(channelUID);
    }

    @Override
    public String toString() {
      return "WatchdogRecovery [" + channelUID + "]";
    }
  }

  public static class WatchdogInitializedEvent extends WatchdogEvent {
    public WatchdogInitializedEvent(ChannelUID channelUID) {
      super(channelUID);
    }

    @Override
    public String toString() {
      return "WatchdogInitialized [" + channelUID + "]";
    }
  }

  public static class WatchdogTimeoutEvent extends WatchdogEvent {
    public WatchdogTimeoutEvent(ChannelUID channelUID) {
      super(channelUID);
    }

    @Override
    public String toString() {
      return "WatchdogTimeout [" + channelUID + "]";
    }
  }
}