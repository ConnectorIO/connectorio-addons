package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.ads.connection.AdsTcpPlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffNetworkConfiguration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BeckhoffNetworkBridgeHandler} is responsible for handling connections to network
 * enabled ADS devices.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class BeckhoffNetworkBridgeHandler extends
    BeckhoffBridgeHandler<AdsTcpPlcConnection, BeckhoffNetworkConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(BeckhoffNetworkBridgeHandler.class);

  public BeckhoffNetworkBridgeHandler(Bridge thing) {
    super(thing);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  protected Runnable createInitializer(CompletableFuture<AdsTcpPlcConnection> initializer) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          BeckhoffNetworkConfiguration config = getBridgeConfig().get();
          String host = hostWithPort(config.host, config.port);
          String target = hostWithPort(config.targetAmsId, config.targetAmsPort);
          String source = config.sourceAmsId != null && config.sourceAmsPort != null ? "/" + hostWithPort(config.sourceAmsId, config.sourceAmsPort) : "";
          AdsTcpPlcConnection connection = (AdsTcpPlcConnection) new PlcDriverManager(getClass().getClassLoader())
              .getConnection("ads:tcp://" + host + "/" + target + source);
          connection.connect();

          if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            initializer.complete(connection);
          } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Connection failed");
            initializer.complete(null);
          }
        } catch (PlcConnectionException e) {
          logger.warn("Could not obtain connection", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
              e.getMessage());
          initializer.completeExceptionally(e);
        }
      }
    };
  }

}
