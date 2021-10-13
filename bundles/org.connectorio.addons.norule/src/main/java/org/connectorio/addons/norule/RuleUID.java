package org.connectorio.addons.norule;

import org.openhab.core.common.AbstractUID;

public class RuleUID extends AbstractUID {

  public RuleUID(String ... segments) {
    super(segments);
  }

  @Override
  protected int getMinimalNumberOfSegments() {
    return 2;
  }

}
