package org.connectorio.addons.managed.xstream;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import org.openhab.core.common.registry.Provider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleProvider<T> implements Provider<T> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final List<ProviderChangeListener<T>> listeners = new CopyOnWriteArrayList<>();
  private final Set<T> elements = new CopyOnWriteArraySet<>();

  public SimpleProvider(Collection<T> elements) {
    this.elements.addAll(elements);
  }

  @Override
  public Collection<T> getAll() {
    return elements;
  }

  public void add(T element) {
    if (elements.add(element)) {
      listeners.forEach(listener -> listener.added(this, element));
    } else {
      logger.warn("Duplicate element added {}, ignoring", element);
    }
  }

  public void update(T oldElement, T element) {
    if (elements.contains(oldElement)) {
      elements.remove(oldElement);
      elements.add(element);
      listeners.forEach(listener -> listener.updated(this, oldElement, element));
    } else {
      add(element);
    }
  }

  public void remove(T element) {
    if (elements.remove(element)) {
      listeners.forEach(listener -> listener.removed(this, element));
    }
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<T> listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<T> listener) {
    this.listeners.remove(listener);
  }

  public void deactivate() {
    for (T element : elements) {
      for (ProviderChangeListener<T> listener : listeners) {
        listener.removed(this, element);
      }
    }
    elements.clear();
  }
}
