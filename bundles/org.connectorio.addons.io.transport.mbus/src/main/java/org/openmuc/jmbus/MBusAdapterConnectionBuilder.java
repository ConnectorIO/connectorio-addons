package org.openmuc.jmbus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.openmuc.jmbus.transportlayer.TransportLayer;

public class MBusAdapterConnectionBuilder {

  public MBusConnection build(TransportLayer transportLayer) throws Exception {
    Constructor<MBusConnection> constructor = MBusConnection.class.getDeclaredConstructor(TransportLayer.class);
    constructor.setAccessible(true);
    MBusConnection connection = constructor.newInstance(transportLayer);

    Method method = MBusConnection.class.getDeclaredMethod("open");
    method.setAccessible(true);
    method.invoke(connection);
    return connection;
  }

}
