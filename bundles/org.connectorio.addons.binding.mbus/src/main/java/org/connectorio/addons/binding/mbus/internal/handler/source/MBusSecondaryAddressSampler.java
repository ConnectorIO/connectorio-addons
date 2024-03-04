package org.connectorio.addons.binding.mbus.internal.handler.source;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.openhab.core.types.State;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.SecondaryAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBusSecondaryAddressSampler extends MBusBaseSampler implements MBusSampler {

  public static final int PRIMARY_ADDRESS_FOR_SECONDARY_READOUT = 0xFD;
  private final Logger logger = LoggerFactory.getLogger(MBusSecondaryAddressSampler.class);
  private final SecondaryAddress secondaryAddress;

  public MBusSecondaryAddressSampler(MBusConnection connection, SecondaryAddress secondaryAddress, Map<ChannelKey, Consumer<DataRecord>> callbacks) {
    super(connection, PRIMARY_ADDRESS_FOR_SECONDARY_READOUT, callbacks);
    this.secondaryAddress = secondaryAddress;
  }

  @Override
  public CompletableFuture<List<DataRecord>> fetch() {
    try {
      connection.selectComponent(secondaryAddress);
      return read();
    } catch (IOException e) {
      logger.warn("Could not select component for readout {}", secondaryAddress, e);
      return CompletableFuture.failedFuture(e);
    } finally {
      try {
        connection.resetReadout(PRIMARY_ADDRESS_FOR_SECONDARY_READOUT);
      } catch (IOException e) {
        logger.warn("Failure while deselecting readout component {}", secondaryAddress, e);
      }
      try {
        connection.deselectComponent();
      } catch (IOException e) {
        logger.warn("Failure while deselecting readout component {}", secondaryAddress, e);
      }
    }
  }

}
