package org.connectorio.addons.managed.widget.internal;

import java.util.Collection;
import java.util.List;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.ui.components.RootUIComponent;
import org.openhab.core.ui.components.UIProvider;

public class StaticUIProvider implements UIProvider {

  private final String namespace;
  private final List<RootUIComponent> components;

  public StaticUIProvider(String namespace, List<RootUIComponent> components) {
    this.namespace = namespace;
    this.components = components;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<RootUIComponent> listener) {

  }

  @Override
  public Collection<RootUIComponent> getAll() {
    return components;
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<RootUIComponent> listener) {

  }
}
