package org.connectorio.addons.managed.item.internal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openhab.core.common.registry.Provider;
import org.openhab.core.common.registry.ProviderChangeListener;

public abstract class SimpleProvider<T> implements Provider<T> {

  private final List<ProviderChangeListener<T>> listeners = new CopyOnWriteArrayList<>();
  private final List<T> elements = new CopyOnWriteArrayList<>();

  public SimpleProvider() {
  }

  public SimpleProvider(List<T> elements) {
    this.elements.addAll(elements);
  }

  @Override
  public Collection<T> getAll() {
    return elements;
  }

  void add(T element) {
    elements.add(element);

    listeners.forEach(listener -> listener.added(this, element));
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<T> listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<T> listener) {
    this.listeners.remove(listener);
  }

}
