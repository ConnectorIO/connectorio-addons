package org.connectorio.binding.plc4x.siemens.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.s7.connection.S7PlcConnection;
import org.connectorio.binding.plc4x.shared.handler.SharedPlc4xBridgeHandler;
import org.connectorio.binding.plc4x.siemens.internal.SiemensBindingConstants;
import org.connectorio.binding.plc4x.siemens.internal.config.SiemensNetworkConfiguration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensNetworkBridgeHandler} is responsible for handling communication with Siemens S7 PLCs over network
 * sockets.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class SiemensNetworkBridgeHandler extends
  SharedPlc4xBridgeHandler<S7PlcConnection, SiemensNetworkConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(SiemensNetworkBridgeHandler.class);

  private CompletableFuture<S7PlcConnection> initializer;

  public SiemensNetworkBridgeHandler(Bridge thing) {
    super(thing);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.UNKNOWN);

    initializer = new CompletableFuture<>();
    Runnable connectionTask = new Runnable() {
      @Override
      public void run() {
        try {
          SiemensNetworkConfiguration config = getBridgeConfig().get();
          S7PlcConnection connection = (S7PlcConnection) new PlcDriverManager(getClass().getClassLoader())
            .getConnection("s7://" + config.host /*+ port*/ + "/" + config.rack + "/" + config.slot);
          connection.connect();

          if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            initializer.complete(connection);
          } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection failed");
            initializer.complete(null);
          }
        } catch (PlcConnectionException e) {
          logger.warn("Could not obtain connection", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
          initializer.completeExceptionally(e);
        }
      }
    };
    scheduler.submit(connectionTask);
  }

  @Override
  public CompletableFuture<S7PlcConnection> getInitializer() {
    return initializer;
  }

  @Override
  public S7PlcConnection getConnection() {
    return initializer.getNow(null);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return SiemensBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
