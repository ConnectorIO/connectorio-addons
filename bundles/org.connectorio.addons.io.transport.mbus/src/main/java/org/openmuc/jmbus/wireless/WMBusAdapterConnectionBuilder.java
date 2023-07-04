package org.openmuc.jmbus.wireless;

import java.io.IOException;
import java.lang.reflect.Field;
import org.openmuc.jmbus.transportlayer.TransportLayer;
import org.openmuc.jmbus.wireless.WMBusConnection.Builder;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusManufacturer;

public class WMBusAdapterConnectionBuilder {

  private final Builder builder;

//  public WMBusAdapterConnectionBuilder(WMBusListener listener, WMBusMode mode) {
//    builder = new Builder(null, listener) {
//      @Override
//      WMBusConnection build(TransportLayer transportLayer) throws IOException {
//        WMBusConnectionCUL connectionCUL = new WMBusConnectionCUL(mode, listener, transportLayer);
//        connectionCUL.open();
//        return connectionCUL;
//      }
//    };
//  }

  public WMBusAdapterConnectionBuilder(WMBusManufacturer wmBusManufacturer, WMBusListener listener) {
    builder = new Builder(wmBusManufacturer, listener);
  }

  public WMBusAdapterConnectionBuilder setMode(WMBusMode mode) {
    try {
      // hack for limited visibility of mode field and lack of proper accessor to specify it
      Field modeField = builder.getClass().getDeclaredField("mode");
      modeField.setAccessible(true);
      modeField.set(builder, mode);
      return this;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public WMBusConnection build(TransportLayer transportLayer) throws IOException {
    return builder.build(transportLayer);
  }

}
