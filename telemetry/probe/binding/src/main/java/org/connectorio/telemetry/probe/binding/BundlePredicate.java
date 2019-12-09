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
