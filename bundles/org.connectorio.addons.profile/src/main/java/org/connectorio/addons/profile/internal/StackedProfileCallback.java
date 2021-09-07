package org.connectorio.addons.profile.internal;

import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class StackedProfileCallback implements ProfileCallback {

  private final static ThreadLocal<ProfileCallback> DELEGATE = new ThreadLocal<>();

  @Override
  public void handleCommand(Command command) {
    getDelegate().handleCommand(command);
  }

  @Override
  public void sendCommand(Command command) {
    getDelegate().sendCommand(command);
  }

  @Override
  public void sendUpdate(State state) {
    getDelegate().sendUpdate(state);
  }

  private ProfileCallback getDelegate() {
    ProfileCallback callback = DELEGATE.get();
    if (callback != null) {
      return callback;
    }

    throw new IllegalStateException("No callback found on thread stack");
  }

  static void set(ProfileCallback callback) {
    DELEGATE.set(callback);
  }

}
