package org.connectorio.addons.binding.canopen.ta.tapi;

import org.connectorio.addons.binding.canopen.api.CoNode;

public class TACanStringPointer {

  short index;
  short subIndex;
  short subIndexMax;

  public TACanStringPointer(CoNode node, int index, int subIndex) {
    node.read((short) index, (short) subIndex).whenComplete((response, error) -> {
      this.index = (short)(((response[3]) << 8) | (response[4]));
      this.subIndex = response[1];
      this.subIndexMax = (short) (response[2] - 1);
    });
  }

  public String toString() {
    String output = "TAStringPointer(";
    if (index != 0) {
      output += index;
    }
    if (subIndex != 0) {
      output += (index != 0 ? ", " : "") + subIndex;
    }
    if (subIndexMax != 0) {
      output += "..." + subIndexMax;
    }
    return output + ")";
  }

}
