package org.connectorio.addons.binding.canbus.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.PlcDriver;
import org.connectorio.addons.binding.canbus.config.SocketCANConfiguration;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.plc4x.extras.osgi.PlcDriverManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

public class SocketCANBridgeHandler extends GenericBridgeHandlerBase<SocketCANConfiguration> {

  private final CompletableFuture<PlcConnection> connection = new CompletableFuture<>();
  private final PlcDriverManager driverManager;

  public SocketCANBridgeHandler(Bridge thing, PlcDriverManager driverManager) {
    super(thing);
    this.driverManager = driverManager;
  }

  @Override
  public void initialize() {
    scheduler.execute(new Runnable() {
      @Override
      public void run() {
        SocketCANConfiguration configuration = getBridgeConfig().get();

        try {
          PlcDriver driver = driverManager.getDriver("genericcan");
          PlcConnection plcConnection = driver.getConnection("genericcan:socketcan:" + configuration.name);
          if (!plcConnection.isConnected()) {
            plcConnection.connect();
          }
          if (plcConnection.isConnected()) {
            connection.complete(plcConnection);
            updateStatus(ThingStatus.ONLINE);
          } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not open connection");
          }
        } catch (Exception e) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
            "Could not initialize handler " + e.getMessage());
        }
      }
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

}
