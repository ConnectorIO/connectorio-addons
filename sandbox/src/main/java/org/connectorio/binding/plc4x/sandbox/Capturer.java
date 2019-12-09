package org.connectorio.binding.plc4x.sandbox;

import com.savarese.rocksaw.net.RawSocket;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.opendaylight.openflowplugin.libraries.liblldp.EtherTypes;
import org.opendaylight.openflowplugin.libraries.liblldp.Ethernet;
import org.opendaylight.openflowplugin.libraries.liblldp.LLDP;
import org.opendaylight.openflowplugin.libraries.liblldp.LLDPTLV;
import org.opendaylight.openflowplugin.libraries.liblldp.NetUtils;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket.EthernetHeader;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Packet.IpV4Header;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.Packet.Header;
import org.pcap4j.packet.namednumber.DataLinkType;
import org.pcap4j.packet.namednumber.EtherType;

public class Capturer {

  public static void main(String[] args) throws Exception {
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

      EtherTypes t = EtherTypes.LLDP;
      EtherType LLDP = new EtherType(t.shortValue(), t.name());

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            PcapNetworkInterface pif = Pcaps.getDevByName(name);
            System.out.println(pif);

            int snapLen = 65536;
            PromiscuousMode mode = PromiscuousMode.PROMISCUOUS;
            int timeout = 10;
            PcapHandle handle = pif.openLive(snapLen, mode, timeout);


//            String filterString = "ip protochain " + protocolNumber +
//                " and ether dst " + localMacAddress.toString() +
//                " and ip dst " + localIpAddress.getHostAddress() +
//                " and ether src " + firstHopMacAddress.toString() +
//                " and ip src " + remoteIpAddress.getHostAddress();
//
//            receiveHandle.setFilter(filterString, BpfProgram.BpfCompileMode.OPTIMIZE);

            Packet packet = null;
            while ((packet = handle.getNextPacketEx()) != null) {
              Header header = packet.getHeader();
              if (header instanceof EthernetHeader) {
                EthernetHeader eh = (EthernetHeader) header;
                if (LLDP.value().equals(eh.getType().value())) {

                  System.out.println("Deserialize LLDP");
                  System.out.println(packet.getClass().getName() + " " + packet);
                  LLDP lldp = new LLDP();
                  Ethernet ethPkt = new Ethernet();
                  byte[] rawData = packet.getPayload().getRawData();
                  org.opendaylight.openflowplugin.libraries.liblldp.Packet deserialize = ethPkt
                      .deserialize(packet.getRawData(), 0,
                          rawData.length * NetUtils.NUM_BITS_IN_A_BYTE);
                  if (deserialize != null) {
                    System.out.println("LLDP packet " + deserialize);
                    LLDP lldp1 = (org.opendaylight.openflowplugin.libraries.liblldp.LLDP) deserialize.getPayload();
                    System.out.println("LLDP packet " + lldp1.getClass());
                    System.out.println("Get chassis " + lldp1.getChassisId());
                    System.out.println("Port descr  " + lldp1.getPortDesc());
                    System.out.println("Port id     " + lldp1.getPortId());
                    System.out.println("Sys capabil " + lldp1.getSystemCapabilities());
                    System.out.println("Sys descr   " + lldp1.getSystemDesc());
                    System.out.println("Sys id      " + lldp1.getSystemNameId());
                    LLDPTLV managementAddress = lldp1.getManagementAddress();
                    System.out.println("Address     " + managementAddress);
                    System.out.println("Payload " + Hex.encodeHexString(managementAddress.getRawPayload()));
                    System.out.println("Payload " + Hex.encodeHexString(managementAddress.getValue()));
                    System.out.println("Address     " + LLDPTLV.getHexStringValue(managementAddress.getValue(), managementAddress.getLength()));
                  }
                } else {
                  //System.out.println(packet.getClass().getName() + " " + packet);
                }
              }
                //        LLDP lldp = new LLDP();
                //        byte[] rawData = packet.getRawData();
                //        org.opendaylight.openflowplugin.libraries.liblldp.Packet deserialize = lldp
                //          .deserialize(rawData, 0, rawData.length);
                //        if (deserialize != null) {
                //          System.out.println("LLDP paclet " + deserialize.toString());
                //        }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }).start();
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
