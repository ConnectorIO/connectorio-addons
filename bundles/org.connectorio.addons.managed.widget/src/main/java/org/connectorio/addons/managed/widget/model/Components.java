package org.connectorio.addons.managed.widget.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Components {

  private List<RootEntry> components;

  public Components() {
    this(new ArrayList<>());
  }

  public Components(List<RootEntry> roots) {
    this.components = roots;
  }

  public List<RootEntry> getComponents() {
    return components;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Components)) {
      return false;
    }
    Components that = (Components) o;
    return Objects.equals(getComponents(), that.getComponents());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getComponents());
  }
}
