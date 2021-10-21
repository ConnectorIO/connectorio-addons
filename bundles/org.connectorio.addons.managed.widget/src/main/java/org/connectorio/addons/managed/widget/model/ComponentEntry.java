package org.connectorio.addons.managed.widget.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openhab.core.ui.components.UIComponent;

public class ComponentEntry {

  private String type;
  private Map<String, Object> config;
  private Map<String, List<ComponentEntry>> slots;

  public Map<String, List<ComponentEntry>> getSlots() {
    return slots;
  }

  public void setSlots(Map<String, List<ComponentEntry>> slots) {
    this.slots = slots;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ComponentEntry)) {
      return false;
    }
    ComponentEntry that = (ComponentEntry) o;
    return Objects.equals(getType(), that.getType()) && Objects.equals(getConfig(), that.getConfig())
      && Objects.equals(getSlots(), that.getSlots());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), getConfig(), getSlots());
  }

}
