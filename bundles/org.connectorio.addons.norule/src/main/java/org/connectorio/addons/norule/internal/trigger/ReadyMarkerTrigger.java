package org.connectorio.addons.norule.internal.trigger;

import org.connectorio.addons.norule.Trigger;
import org.openhab.core.service.ReadyMarker;

public abstract class ReadyMarkerTrigger implements Trigger {

  private final ReadyMarker marker;

  public ReadyMarkerTrigger(ReadyMarker marker) {
    this.marker = marker;
  }

  public ReadyMarker getMarker() {
    return marker;
  }

}
