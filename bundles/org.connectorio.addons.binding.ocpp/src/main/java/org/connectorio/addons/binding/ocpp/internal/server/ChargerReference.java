package org.connectorio.addons.binding.ocpp.internal.server;

import java.util.Objects;

public class ChargerReference {

  private final String serial;

  public ChargerReference(String serial) {
    this.serial = serial;
  }

  public String getSerial() {
    return serial;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChargerReference)) {
      return false;
    }
    return Objects.equals(serial, ((ChargerReference) o).serial);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(serial);
  }

  public String toString() {
    return "Charger [SN: " + serial + "]";
  }

}
