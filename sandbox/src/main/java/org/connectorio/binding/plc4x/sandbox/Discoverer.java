package org.connectorio.binding.plc4x.sandbox;

import com.savarese.rocksaw.net.RawSocket;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.connectorio.binding.plc4x.sandbox.jna.CapNG;
import org.connectorio.binding.plc4x.sandbox.jna.JNAApiInterface;
import org.opendaylight.openflowplugin.libraries.liblldp.Ethernet;
import org.opendaylight.openflowplugin.libraries.liblldp.LLDP;

public class Discoverer {

  public static void main(String[] args) throws Exception {
    System.load("/home/splatch/projects/rocksaw/target/native/librocksaw.so");

    CapNG capNG = CapNG.INSTANCE;
    System.out.println(capNG.capng_get_caps_process());

    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    while (interfaces.hasMoreElements()) {
      NetworkInterface nif = interfaces.nextElement();

      if (nif.isLoopback())
        continue;
      if (nif.isVirtual())
        continue;
      if (!nif.isUp())
        continue;

      String name = nif.getName();
      if (!name.startsWith("ens") && !name.startsWith("eno"))
        continue;

      System.out.println("Send packet via " + name);
      Enumeration<InetAddress> addresses = nif.getInetAddresses();

      System.out.println("Interface addresses");
      List<InterfaceAddress> ifadr = nif.getInterfaceAddresses();
      for (InterfaceAddress a : ifadr) {
        if (a.getBroadcast() != null) {
          send(name, a.getBroadcast());
        }
      }

      InetAddress naddr = addresses.nextElement();
      if (!naddr.isLoopbackAddress() && !(naddr instanceof Inet6Address)) {
        send(name, naddr);
      }
    }
  }

  private static void send(String name, InetAddress naddr) throws IOException {
    // ETH_P_LLDP
    int icmp = RawSocket.getProtocolByName("IPv4");
    System.out.println("Send via " + naddr + "  Protocol number " + icmp);
    RawSocket socket = new RawSocket();
    socket.open(RawSocket.PF_INET, icmp);
    socket.setIPHeaderInclude(true);
    socket.setReceiveBufferSize(100);
    socket.setSendTimeout(100);
    //socket.bindDevice(nif.getName());


    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          byte[] data = new byte[100];
          int len = 0;
          while ((len = socket.read(data)) != -1) {
//                  if (len > 22) {
              byte[] encode = Base64.getEncoder().encode(data);
              System.out.println(len + ": " + name + " " + new String(encode));
//                  }
            Thread.sleep(1000);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();

    byte[] d = new byte[] {
        0x00, 0x04, 0x00, 0x01, 0x00, 0x06, 0x08, 0x00, 0x27, 0x10, (byte) 0xff, 0x10, 0x00, 0x00, (byte) 0x88, (byte) 0x92,
        (byte) 0xfe, (byte) 0xfe, 0x05, 0x00, 0x05, 0x01, 0x00, 0x0d, 0x00, (byte) 0x80, 0x00, 0x04, (byte) 0xff, (byte) 0xff, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    socket.write(naddr, d, 0, d.length);
    socket.close();
  }

}
