package org.connectorio.addons.binding.plc4x.canopen.ta.tapi;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TACanString {

  private final Logger logger = LoggerFactory.getLogger(TACanString.class);
  private final short index;
  private final short subIndex;
  private final CompletableFuture<String> future;

  String value;

  public TACanString(CoNode node, short index, short subIndex) {
    this.index = index;
    this.subIndex = subIndex;
    this.future = node.read(index, subIndex)
      .thenApply(value -> new String(value, StandardCharsets.UTF_16LE))
      .whenComplete((response, error) -> {
      if (error == null) {
        this.value = response;
      } else {
        if (logger.isInfoEnabled()) {
          logger.debug("Failed to load string 0x{}/0x{}", Integer.toHexString(index), Integer.toHexString(subIndex));
        }
        if (logger.isTraceEnabled()) {
          logger.trace("Failed to load string 0x{}/0x{}", Integer.toHexString(index), Integer.toHexString(subIndex), error);
        }
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

  public CompletableFuture<String> toFuture() {
    return future;
  }

}
