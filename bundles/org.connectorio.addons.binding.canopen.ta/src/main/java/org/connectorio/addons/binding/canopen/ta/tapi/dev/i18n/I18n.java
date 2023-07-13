/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.canopen.ta.tapi.dev.i18n;

import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.ta.tapi.TACanString;

public class I18n {

  static String ENGLISH = "English";
  static String GERMAN = "Deutsch";

  public static CompletableFuture<Language> get(CoNode node) {
    return new TACanString(node, 0x2510, 0x03).toFuture()
      .thenApply(language -> {
        if (ENGLISH.equals(language)) {
          return new EnglishLanguage();
        } else if (GERMAN.equals(language)) {
          return new DeutscheLanguage();
        }
        throw new IllegalArgumentException("Unsupported language " + language);
      });
  }

}
