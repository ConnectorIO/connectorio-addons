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
package org.connectorio.telemetry.probe.binding;

import java.util.function.Predicate;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

public interface BundlePredicate extends Predicate<Bundle> {

  class SymbolicName implements BundlePredicate {
    private final Predicate<String> predicate;

    public SymbolicName(Predicate<String> predicate) {
      this.predicate = predicate;
    }

    @Override
    public boolean test(Bundle bundle) {
      return predicate.test(bundle.getSymbolicName());
    }
  }

  class Location implements BundlePredicate {
    private final Predicate<String> predicate;

    public Location(Predicate<String> predicate) {
      this.predicate = predicate;
    }

    @Override
    public boolean test(Bundle bundle) {
      return predicate.test(bundle.getLocation());
    }
  }

  class Name implements BundlePredicate {
    private final Predicate<String> predicate;

    public Name(Predicate<String> predicate) {
      this.predicate = predicate;
    }

    @Override
    public boolean test(Bundle bundle) {
      return predicate.test(bundle.getHeaders().get(Constants.BUNDLE_NAME));
    }
  }

}
