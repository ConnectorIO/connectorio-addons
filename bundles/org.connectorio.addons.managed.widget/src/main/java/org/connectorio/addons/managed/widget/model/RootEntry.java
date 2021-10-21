package org.connectorio.addons.managed.widget.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openhab.core.config.core.dto.ConfigDescriptionDTO;

public class RootEntry extends ComponentEntry {

  private String uid;
  private Set<String> tags = new HashSet<String>();
  private ConfigDescriptionDTO props;
  private Date timestamp;

  public String getUID() {
    return uid;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  public ConfigDescriptionDTO getProps() {
    return props;
  }

  public void setProps(ConfigDescriptionDTO props) {
    this.props = props;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RootEntry)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    RootEntry rootEntry = (RootEntry) o;
    return Objects.equals(getUid(), rootEntry.getUid()) && Objects.equals(getTags(), rootEntry.getTags())
      && Objects.equals(getProps(), rootEntry.getProps()) && Objects.equals(getTimestamp(), rootEntry.getTimestamp());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getUid(), getTags(), getProps(), getTimestamp());
  }
}
