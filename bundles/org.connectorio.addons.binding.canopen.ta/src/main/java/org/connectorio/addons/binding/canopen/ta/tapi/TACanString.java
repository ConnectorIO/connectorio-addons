package org.connectorio.addons.binding.canopen.ta.tapi;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.canopen.readwrite.CANOpenDataType;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TACanString extends TACanValue<String> {

  private final Logger logger = LoggerFactory.getLogger(TACanString.class);

  public TACanString(CoNode node, int index, int subIndex) {
    this(node, (short) index, (short) subIndex);
  }

  public TACanString(CoNode node, short index, short subIndex) {
    super(node, index, subIndex, CANOpenDataType.RECORD);
  }

  protected CompletableFuture<String> initialize(CompletableFuture<byte[]> future) {
    return future.thenApply(this::strip)
      .thenApply(value -> new String(value, StandardCharsets.UTF_16LE).trim())
      .whenComplete((response, error) -> {
        if (error != null) {
          if (logger.isInfoEnabled()) {
            logger.debug("Failed to load string 0x{}/0x{}", Integer.toHexString(index), Integer.toHexString(subIndex));
          }
          if (logger.isTraceEnabled()) {
            logger.trace("Failed to load string 0x{}/0x{}", Integer.toHexString(index), Integer.toHexString(subIndex), error);
          }
          return;
        }
        this.value = response;
      });
  }

  private byte[] strip(byte[] bytes) {
    if (bytes.length < 4) {
      return new byte[0];
    }

    if (bytes[0] != 0x1F) {
      logger.warn("Unexpected header for text value {}. Ignoring value {}", Integer.toHexString(bytes[0]), Hex.encodeHexString(bytes));
      return new byte[0];
    }

    // remove two first bytes of text.
    byte[] text = new byte[bytes.length - 2];
    System.arraycopy(bytes, 2, text, 0, bytes.length - 2);
    return text;
  }

}
