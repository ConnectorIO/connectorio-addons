package org.connectorio.telemetry.probe.binding;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.connectorio.telemetry.api.Probe;
import org.connectorio.telemetry.model.Telemetry;
import org.connectorio.telemetry.model.v1.TelemetryV1;
import org.connectorio.telemetry.probe.binding.BundlePredicate.Location;
import org.connectorio.telemetry.probe.binding.BundlePredicate.Name;
import org.connectorio.telemetry.probe.binding.BundlePredicate.SymbolicName;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

@Component(service = Probe.class)
public class BindingProbe implements Probe {

  private final BundleContext context;

  public BindingProbe(BundleContext context) {
    this.context = context;
  }

  @Override
  public Telemetry report() {
    Predicate<String> filter = (string -> string.toLowerCase().contains("openhab") && string.toLowerCase().contains("binding"));
    Predicate<Bundle> predicate = new Name(filter).or(new Location(filter)).or(new SymbolicName(filter));

    Map<String, String> bindings = Arrays.stream(context.getBundles())
      .filter(predicate)
      .collect(Collectors.toMap(Bundle::getSymbolicName, Bundle::getLocation));

    return new TelemetryV1("org.connectorio/bindings/installed", bindings);
  }

}
