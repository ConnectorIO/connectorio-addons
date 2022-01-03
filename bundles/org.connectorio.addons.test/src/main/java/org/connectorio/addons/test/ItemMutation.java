package org.connectorio.addons.test;

import java.util.function.Consumer;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.types.State;

public class ItemMutation implements Consumer<State> {

  private final Item item;

  public ItemMutation(Item item) {
    this.item = item;
  }

  @Override
  public void accept(State state) {
    if (item instanceof GenericItem) {
      ((GenericItem) item).setState(state);
    }
  }

}
