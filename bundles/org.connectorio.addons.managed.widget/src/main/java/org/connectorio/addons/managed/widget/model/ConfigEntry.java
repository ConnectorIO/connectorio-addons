package org.connectorio.addons.managed.widget.model;

import java.util.List;
import java.util.Objects;
import org.openhab.core.config.core.dto.ConfigDescriptionParameterDTO;
import org.openhab.core.config.core.dto.ConfigDescriptionParameterGroupDTO;

public class ConfigEntry {

  String uri;
  List<ConfigDescriptionParameterDTO> parameters;
  List<ConfigDescriptionParameterGroupDTO> groups;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConfigEntry)) {
      return false;
    }
    ConfigEntry that = (ConfigEntry) o;
    return Objects.equals(uri, that.uri) && Objects.equals(parameters, that.parameters) && Objects.equals(
      groups, that.groups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri, parameters, groups);
  }
}
