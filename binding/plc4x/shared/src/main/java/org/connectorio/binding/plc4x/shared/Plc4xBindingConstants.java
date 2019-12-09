package org.connectorio.binding.plc4x.shared;

import org.connectorio.binding.base.BaseBindingConstants;

public interface Plc4xBindingConstants extends BaseBindingConstants {

  String PREFIX = "plc4x";

  static String protocol(String protocol) {
    return BaseBindingConstants.identifier(PREFIX, protocol);
  }

}
