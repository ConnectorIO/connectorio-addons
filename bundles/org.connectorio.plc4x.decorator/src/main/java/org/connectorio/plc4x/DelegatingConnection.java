package org.connectorio.plc4x;

import org.apache.plc4x.java.api.PlcConnection;

public interface DelegatingConnection extends PlcConnection {

  PlcConnection getDelegate();

  <T extends PlcConnection> T cast(Class<T> type);

}
