package org.connectorio.addons.binding.mbus.internal.handler.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.openhab.core.types.State;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.VariableDataStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MBusBaseSampler implements MBusSampler {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final MBusConnection connection;
  protected final int primaryAddress;
  private final Map<ChannelKey, Consumer<DataRecord>> callbacks;

  protected MBusBaseSampler(MBusConnection connection, int primaryAddress, Map<ChannelKey, Consumer<DataRecord>> callbacks) {
    this.connection = connection;
    this.primaryAddress = primaryAddress;
    this.callbacks = callbacks;
  }

  protected final CompletableFuture<List<DataRecord>> read() {
    try {
      List<DataRecord> records = new ArrayList<>();
      VariableDataStructure vdr;
      do {
        vdr = connection.read(primaryAddress);
        records.addAll(vdr.getDataRecords());
      } while(vdr.moreRecordsFollow());
      return CompletableFuture.completedFuture(records).whenComplete((result, error) -> {
        if (error != null) {
          logger.debug("Failure while reading out meter {}", primaryAddress, error);
          return;
        }
        for (DataRecord record : result) {
          Consumer<DataRecord> consumer = callbacks.get(new ChannelKey(record.getDib(), record.getVib()));
          if (consumer != null) {
            consumer.accept(record);
          }
        }
      });
    } catch (IOException e) {
      return CompletableFuture.failedFuture(e);
    } finally {
      try {
        connection.resetReadout(primaryAddress);
      } catch (IOException e) {
        logger.warn("Could not reset readout for device {}", primaryAddress, e);
      }
    }
  }
}
