package org.connectorio.binding.plc4x.beckhoff.internal.config;

import org.connectorio.binding.base.config.PollingConfiguration;

public class BeckhoffBridgeConfiguration extends PollingConfiguration {

  public String targetAmsId;
  public Integer targetAmsPort;
  public String sourceAmsId;
  public Integer sourceAmsPort;

}
