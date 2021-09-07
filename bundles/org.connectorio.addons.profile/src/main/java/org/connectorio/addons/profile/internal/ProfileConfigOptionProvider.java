package org.connectorio.addons.profile.internal;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeRegistry;
import org.openhab.core.thing.profiles.StateProfileType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ConfigOptionProvider.class)
public class ProfileConfigOptionProvider implements ConfigOptionProvider {

  private static final String CONFIG_URI = "profile:connectorio:profiles";
  private static final String PROFILES_OPTION = "profiles";
  private final ProfileTypeRegistry registry;
  private Set<ProfileFactory> profileFactories;

  @Activate
  public ProfileConfigOptionProvider(@Reference ProfileTypeRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Collection<ParameterOption> getParameterOptions(URI uri, String param, String context, Locale locale) {
    if (CONFIG_URI.equals(uri.toString()) && PROFILES_OPTION.equals(param)) {
      Set<ParameterOption> options = new HashSet<>();
      for (ProfileType type : registry.getProfileTypes(locale)) {
        if (ConnectorioProfiles.PROFILE.equals(type.getUID())) {
          // remove chain from options
          continue;
        }
        if (type instanceof StateProfileType) {
          options.add(new ParameterOption(type.getUID().getAsString(), type.getLabel()));
        }
      }
      return options;
    }
    return null;
  }
}
