package org.connectorio.addons.network;

import org.openhab.core.common.AbstractUID;

public class NetworkUID extends AbstractUID {

  public NetworkUID(String... segments) {
    super(segments);
  }

  @Override
  protected int getMinimalNumberOfSegments() {
    return 2;
  }

  public String getType() {
    return getSegment(0);
  }

}
