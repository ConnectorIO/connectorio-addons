package org.connectorio.binding.base;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Top level binding constants.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public interface BaseBindingConstants {

  String PREFIX = "co7io";
  String SEPARATOR = "-";

  static String identifier(String ... parts) {
    String[] fullPath = new String[parts.length + 1];
    fullPath[0] = PREFIX;
    System.arraycopy(parts, 0, fullPath, 1, parts.length);

    return Arrays.stream(fullPath)
      .collect(Collectors.joining(SEPARATOR));
  }

}
