/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
