/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.fatek.internal.discovery.broadcast;

public class DiscoveryPacket {

  private double firmwareVersion;
  private String macAddress;
  private String ip;
  private String gateway;
  private String subnet;
  private String plcName;
  private String plcDescription;
  private short primaryFaconPort;
  private short primaryModbusPort;

  public void setFirmwareVersion(double firmwareVersion) {
    this.firmwareVersion = firmwareVersion;
  }

  public double getFirmwareVersion() {
    return firmwareVersion;
  }


  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getIp() {
    return ip;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public String getGateway() {
    return gateway;
  }

  public void setSubnet(String subnet) {
    this.subnet = subnet;
  }

  public String getSubnet() {
    return subnet;
  }

  public void setPlcName(String plcName) {
    this.plcName = plcName;
  }

  public String getPlcName() {
    return plcName;
  }

  public void setPlcDescription(String plcDescription) {
    this.plcDescription = plcDescription;
  }

  public String getPlcDescription() {
    return plcDescription;
  }

  public void setPrimaryFaconPort(short primaryFaconPort) {
    this.primaryFaconPort = primaryFaconPort;
  }

  public short getPrimaryFaconPort() {
    return primaryFaconPort;
  }

  public void setPrimaryModbusPort(short primaryModbusPort) {
    this.primaryModbusPort = primaryModbusPort;
  }

  public short getPrimaryModbusPort() {
    return primaryModbusPort;
  }
}
