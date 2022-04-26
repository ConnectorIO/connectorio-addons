package org.connectorio.addons.binding.smartme;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.connectorio.addons.binding.smartme.v1.ApiClient;
import org.connectorio.addons.binding.smartme.v1.client.DevicesApi;
import org.connectorio.addons.binding.smartme.v1.client.MeterValuesApi;
import org.connectorio.addons.binding.smartme.v1.client.UserApi;
import org.connectorio.addons.binding.smartme.v1.client.ValuesApi;
import org.connectorio.addons.binding.smartme.v1.client.ValuesInPastApi;
import org.connectorio.addons.binding.smartme.v1.client.model.Device;
import org.connectorio.addons.binding.smartme.v1.client.model.DeviceInPast;
import org.connectorio.addons.binding.smartme.v1.client.model.ValuesData;

public class Main {

  public static void main(String[] args) throws Exception {
    PasswordAuthentication passwordAuthentication = new PasswordAuthentication(
        "9494030300@emob.neovac.ch", "9494030300".toCharArray());
    ApiClient client = new ApiClient();
    Builder foo = HttpClient.newBuilder().authenticator(new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        ;
        return passwordAuthentication;
      }
    }
    );
    client.setRequestInterceptor(new Consumer<HttpRequest.Builder>() {
      @Override
      public void accept(HttpRequest.Builder builder) {
        builder.header("Authorization",
          "Basic " + Base64.getEncoder().encodeToString((passwordAuthentication.getUserName() + ":" + new String(passwordAuthentication.getPassword())).getBytes())
        );
      }
    });
    client.setResponseInterceptor(new Consumer<HttpResponse<InputStream>>() {
      @Override
      public void accept(HttpResponse<InputStream> inputStreamHttpResponse) {
        // [{"Id":"2acd74e0-ab66-6c37-17bf-a734f3b4a034","Name":"#1 Parkplatz oben (#1)","Serial":-231328692,"DeviceEnergyType":1,"MeterSubType":3,"FamilyType":1001,"ActivePower":0.0,"ActivePowerUnit":"kW","CounterReading":129.214,"CounterReadingUnit":"kWh","CounterReadingT1":129.214,"CounterReadingImport":129.214,"ValueDate":"2022-04-12T16:52:55.1210809Z"},{"Id":"5f448f0c-f749-614c-4a48-fc0c85d93587","Name":"#2 Parkplatz unten rechts (#2)","Serial":-41733850,"DeviceEnergyType":1,"MeterSubType":3,"FamilyType":1001,"ActivePower":0.0,"ActivePowerUnit":"kW","CounterReading":37.959,"CounterReadingUnit":"kWh","CounterReadingT1":37.959,"CounterReadingImport":37.959,"ValueDate":"2022-04-12T16:52:58.1432052Z"},{"Id":"be4ba20f-1a01-cb3a-8ce9-f1a56238ba4f","Name":"#3 Parkplatz unten links (#3 )","Serial":-985057969,"DeviceEnergyType":1,"MeterSubType":3,"FamilyType":1001,"ActivePower":0.0,"ActivePowerUnit":"kW","CounterReading":0.0,"CounterReadingUnit":"kWh","CounterReadingT1":0.0,"CounterReadingImport":0.0,"ValueDate":"2022-04-12T16:52:51.0550007Z"}]
//        String text = new BufferedReader(
//            new InputStreamReader(inputStreamHttpResponse.body(), StandardCharsets.UTF_8))
//            .lines()
//            .collect(Collectors.joining("\n"));
//        System.out.println(text);
      }
    });
    client.setHttpClientBuilder(foo);

    get(() -> new UserApi(client).userGet());

    List<Device> devices = new DevicesApi(client).devicesGet();
    for (Device device : devices) {
      System.out.println(device.getId() + " " + device.getName());
//      get(() -> new ValuesApi(client).valuesGet(device.getId()));
//      get(() -> new MeterValuesApi(client).meterValuesGet(device.getId(), OffsetDateTime.now()));
//      get(() -> new ValuesInPastApi(client).valuesInPastGet(device.getId(), OffsetDateTime.now()));
      get(() -> new DevicesApi(client).devicesGet_0(device.getId()));
    }
  }

  private static <X> void get(Callable<X> client) {
    try {
      System.out.println(client.call());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
