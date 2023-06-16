package org.connectorio.addons.network.iface;

import java.util.Arrays;
import org.openhab.core.common.AbstractUID;

public class NetworkInterfaceUID extends AbstractUID {

  public NetworkInterfaceUID(String uid) {
    this(uid.split(SEPARATOR));
  }

  public NetworkInterfaceUID(String... segments) {
    super(Arrays.asList(segments));
  }

  @Override
  protected int getMinimalNumberOfSegments() {
    return 2;
  }

  public String getType() {
    return getSegment(0);
  }
  public String getName() {
    return getSegment(1);
  }

}
