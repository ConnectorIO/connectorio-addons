package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.publisher;

import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.values.PlcSINT;
import org.apache.plc4x.java.spi.values.PlcValues;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PublishingRunnable implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final CoNode node;

  protected PublishingRunnable(CoNode node) {
    this.node = node;
  }

  protected final void send(int nodeId, CANOpenService service, WriteBuffer buffer) {
    byte[] data = buffer.getData();
    logger.trace("Send to node {} {} (cob {}) data: {}", nodeId, service, Integer.toHexString(service.getMin() + nodeId), Hex.encodeHexString(data));
    node.getConnection().send(nodeId, service, PlcValues.of(
      new PlcSINT(data[0]), new PlcSINT(data[1]), new PlcSINT(data[2]), new PlcSINT(data[3]),
      new PlcSINT(data[4]), new PlcSINT(data[5]), new PlcSINT(data[6]), new PlcSINT(data[7])
    ));
  }
}
