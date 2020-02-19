package org.connectorio.persistence.api;

import org.eclipse.smarthome.core.items.Item;

public interface Operation<T> {

  T execute(Item item);

}
