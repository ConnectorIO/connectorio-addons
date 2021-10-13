package org.connectorio.addons.norule.internal.trigger;

import org.connectorio.addons.norule.Trigger;

public class StartLevelTrigger implements Trigger {

  private final int level;

  public StartLevelTrigger(int level) {
    this.level = level;
  }

  public int getStartLevel() {
    return 0;
  }
}
