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
    this.future = initialize(node);
  }

  protected CompletableFuture<String> initialize(CoNode node) {
    return node.read(index, subIndex)
      .thenApply(this::strip)
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


  private byte[] strip(byte[] bytes) {
    if (bytes.length < 4) {
      return new byte[0];
    }

    // remove two first bytes of text and two termination bytes from the end (0x0000).
    byte[] text = new byte[bytes.length - 4];
    logger.trace("Text base 0x{} {}, text end 0x{} {}", Integer.toHexString(bytes[0]), Integer.toHexString(bytes[1]),
      Integer.toHexString(bytes[bytes.length - 2]), Integer.toHexString(bytes[bytes.length - 1]));
    System.arraycopy(bytes, 2, text, 0, bytes.length - 4);
    return text;
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

  public static TACanString empty(CoNode node) {
    return new TACanString(node, (short) 0, (short) 0) {
      @Override
      protected CompletableFuture<String> initialize(CoNode node) {
        return CompletableFuture.completedFuture("");
      }
    };
  }

}
