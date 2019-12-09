package org.connectorio.binding.plc4x.sandbox;

import java.util.concurrent.TimeUnit;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.metadata.PlcConnectionMetadata;

public class Reader {

  public static void main(String[] args) throws Exception {
    PlcDriverManager manager = new PlcDriverManager();
    PlcConnection connection = manager.getConnection("s7://192.168.2.241/0/1");

    connection.connect();
    PlcConnectionMetadata metadata = connection.getMetadata();
    System.out.println(metadata.canRead() + " " + metadata.canWrite() + " " + metadata.canSubscribe());

    read(connection);
    write(connection, true);
    read(connection);
    write(connection, false);
    read(connection);

    while(true) {
      read2(connection);
    }
  }

  private static void write(PlcConnection connection, boolean value)
      throws InterruptedException, java.util.concurrent.ExecutionException {
    PlcWriteResponse c0 = connection.writeRequestBuilder()
        .addItem("c0", "%I0.0:BOOL", value)
        .build().execute().get();
    System.err.println(c0.getResponseCode("c0"));
  }

  private static void read(PlcConnection connection)
      throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
    PlcReadRequest plcReadRequest = createRequest(connection);

    System.out.println("Request data");
    System.out.println(plcReadRequest.getFieldNames());
    PlcReadResponse response = plcReadRequest.execute().get(1000, TimeUnit.MILLISECONDS);

    System.out.println("Response");
    for (String field : response.getFieldNames()) {
      System.out.println(
          field + " " + response.getObject(field) + " " + response.getObject(field).getClass()
              .getName());
    }
  }

  private static PlcReadRequest createRequest(PlcConnection connection) {
    return connection.readRequestBuilder()
          .addItem("w64", "%IW64:INT")
          .addItem("w66", "%IW66:INT")

          .addItem("q0", "%Q0.0:BOOL")
          .addItem("q1", "%Q0.1:BOOL")
          .addItem("q3", "%Q0.3:BOOL")

          .addItem("c0", "%I0.0:BOOL")
          .build();
  }

  private static void read2(PlcConnection connection) throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
    PlcReadRequest plcReadRequest = createRequest(connection);

    PlcReadResponse response = plcReadRequest.execute().get(1000, TimeUnit.MILLISECONDS);

    for (String field : response.getFieldNames()) {
      System.out.print(response.getObject(field) + ",");
    }
    System.out.println();
  }

}
