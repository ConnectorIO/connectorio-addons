package org.connectorio.addons.profile.internal;

import java.util.Iterator;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class ChainedProfileCallback implements ProfileCallback {

  private final Iterator<StateProfile> profiles;
  private final ProfileCallback delegate;

  public ChainedProfileCallback(Iterator<StateProfile> profiles, ProfileCallback delegate) {
    this.profiles = profiles;
    this.delegate = delegate;
  }

  @Override
  public void handleCommand(Command command) {
    if (profiles.hasNext()) {
      profiles.next().onCommandFromItem(command);
    } else {
      delegate.handleCommand(command);
    }
  }

  @Override
  public void sendCommand(Command command) {
    if (profiles.hasNext()) {
      profiles.next().onCommandFromHandler(command);
    } else {
      delegate.sendCommand(command);
    }
  }

  @Override
  public void sendUpdate(State state) {
    if (profiles.hasNext()) {
      profiles.next().onStateUpdateFromHandler(state);
    } else {
      delegate.sendUpdate(state);
    }
  }
}
