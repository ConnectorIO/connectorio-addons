package org.connectorio.addons.norule.internal.trigger;

public abstract class BaseItemTrigger {

  public final static String ANY = "<any>";
  private final String item;

  protected BaseItemTrigger(String item) {
    this.item = item;
  }

  public boolean isAny() {
    return ANY.equals(getItemName());
  }

  public String getItemName() {
    return item;
  }

  public boolean matches(String itemName) {
    return isAny() || itemName.equals(item);
  }
}
