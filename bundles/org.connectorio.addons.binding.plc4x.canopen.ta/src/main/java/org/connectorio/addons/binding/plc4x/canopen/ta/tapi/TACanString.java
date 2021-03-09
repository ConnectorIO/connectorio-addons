package org.connectorio.addons.binding.plc4x.canopen.ta.tapi;

import java.nio.charset.StandardCharsets;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TACanString {

  private final Logger logger = LoggerFactory.getLogger(TACanString.class);
  private final short index;
  private final short subIndex;

  String value;

  public TACanString(CoNode node, short index, short subIndex) {
    this.index = index;
    this.subIndex = subIndex;
    node.read(index, subIndex).whenComplete((response, error) -> {
      if (error == null) {
        this.value = new String(response, StandardCharsets.UTF_16LE);
      } else {
        logger.debug("Failed to load string 0x{}/0x{}", Integer.toHexString(index), Integer.toHexString(subIndex), error);
      }
    });
  }

  public String get() {
    // set only if SDO retrieval succeeded
    return value;
  }

  public String toString() {
    String output = "TAString(";
    if (value != null) {
      output += value + ") [";
    } else {
      output += "unresolved) [";
    }

    return output + (Integer.toHexString(index) + ", " + Integer.toHexString(subIndex) + "]");
  }

}
