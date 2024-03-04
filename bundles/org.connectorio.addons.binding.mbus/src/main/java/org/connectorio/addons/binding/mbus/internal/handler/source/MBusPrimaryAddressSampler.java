package org.connectorio.addons.binding.mbus.internal.handler.source;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.MBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBusPrimaryAddressSampler extends MBusBaseSampler implements MBusSampler {

  private final Logger logger = LoggerFactory.getLogger(MBusPrimaryAddressSampler.class);

  public MBusPrimaryAddressSampler(MBusConnection connection, int address, Map<ChannelKey, Consumer<DataRecord>> callbacks) {
    super(connection, address, callbacks);
  }

  @Override
  public CompletableFuture<List<DataRecord>> fetch() {
    try {
      return read();
    } finally {
      try {
        connection.resetReadout(primaryAddress);
      } catch (IOException e) {
        logger.warn("Could not reset readout for device {}", primaryAddress, e);
      }
    }
  }

}
