
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.code_house.bacnet4j.wrapper.api.Type;
import org.code_house.bacnet4j.wrapper.ip.BacNetIpClient;
import org.connectorio.addons.binding.bacnet.internal.handler.object.task.ReadObjectTask;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class ReadObjectTaskTest {

  public static void main(String[] args) throws Exception {
    BacNetIpClient client = new BacNetIpClient("192.168.2.255", 3210);
    client.start();

    final ThingHandlerCallback callback = new ThingHandlerCallback() {

      @Override
      public void stateUpdated(ChannelUID channelUID, State state) {
        System.out.println("Channel " + channelUID + " updated to " + state);
      }

      @Override
      public void postCommand(ChannelUID channelUID, Command command) {

      }

      @Override
      public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {

      }

      @Override
      public void thingUpdated(Thing thing) {

      }

      @Override
      public void validateConfigurationParameters(Thing thing,
          Map<String, Object> configurationParameters) {

      }

      @Override
      public void configurationUpdated(Thing thing) {

      }

      @Override
      public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID,
          Configuration configuration) {

      }

      @Override
      public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {

      }

      @Override
      public ChannelBuilder createChannelBuilder(ChannelUID channelUID,
          ChannelTypeUID channelTypeUID) {
        return null;
      }

      @Override
      public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
        return null;
      }

      @Override
      public List<ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID,
          ChannelGroupTypeUID channelGroupTypeUID) {
        return null;
      }

      @Override
      public boolean isChannelLinked(ChannelUID channelUID) {
        return false;
      }

      @Override
      public Bridge getBridge(ThingUID bridgeUID) {
        return null;
      }

      // OH 3.3
      public ConfigDescription getConfigDescription(ThingTypeUID thingTypeUID) {
        return null;
      }
      public ConfigDescription getConfigDescription(ChannelTypeUID channelTypeUID) {
        return null;
      }
      public void validateConfigurationParameters(Channel channel, Map<String, Object> config) {
      }
    };

    client.discoverDevices(d -> {
      System.out.println(d.getName() + " " + d.getInstanceNumber());

      List<BacNetObject> objects = client.getDeviceObjects(d);
      for (BacNetObject object : objects) {
        if (object.getType() == Type.SCHEDULE) {


          // Supplier<CompletableFuture<BacNetClient>> client, ThingHandlerCallback callback, BacNetObject object, Set<ChannelUID> channels
          System.out.println("Fetch state of schedule");

          Set<ChannelUID> channels = new LinkedHashSet<>(Arrays.asList(
            new ChannelUID("d1:d1:d1:" + PropertyIdentifier.effectivePeriod.toString()),
            new ChannelUID("d1:d1:d1:" + PropertyIdentifier.weeklySchedule.toString()),
            new ChannelUID("d1:d1:d1:" + PropertyIdentifier.exceptionSchedule.toString()),
            new ChannelUID("d1:d1:d1:" + PropertyIdentifier.listOfObjectPropertyReferences.toString())
          ));

          ReadObjectTask task = new ReadObjectTask(
              () -> CompletableFuture.completedFuture(client), callback, object,
              channels
          );
          task.run();


        }
      }
    }, 60_000);


    System.in.read();

    client.stop();
  }

}
