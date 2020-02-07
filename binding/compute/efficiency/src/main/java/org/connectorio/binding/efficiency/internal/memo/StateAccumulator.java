package org.connectorio.binding.efficiency.internal.memo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.smarthome.core.events.AbstractTypedEventSubscriber;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.osgi.service.component.annotations.Component;

@Component(service = EventSubscriber.class, immediate = true)
public class StateAccumulator extends AbstractTypedEventSubscriber<ItemStateChangedEvent> implements EventSubscriber,
  StateCollector<ItemStateChangedEvent> {

  private final List<StateReceiver<ItemStateChangedEvent>> receivers = new CopyOnWriteArrayList<>();

  public StateAccumulator() {
    super(ItemStateChangedEvent.TYPE);
  }

  @Override
  protected void receiveTypedEvent(ItemStateChangedEvent event) {
    receivers.forEach(receiver -> receiver.accept(event));
  }

  @Override
  public void addStateReceiver(StateReceiver<ItemStateChangedEvent> receiver) {
    receivers.add(receiver);
  }

  @Override
  public void removeStateReceiver(StateReceiver<ItemStateChangedEvent> receiver) {
    receivers.add(receiver);
  }

}
