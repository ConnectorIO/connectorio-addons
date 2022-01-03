package org.connectorio.addons.test;

import java.util.Set;
import java.util.function.BiConsumer;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;

public class SubscriberCaller implements BiConsumer<Event, EventSubscriber[]> {

  @Override
  public void accept(Event event, EventSubscriber[] eventSubscribers) {
    for (EventSubscriber subscriber : eventSubscribers) {
      Set<String> subscribedEventTypes = subscriber.getSubscribedEventTypes();
      if (subscribedEventTypes.isEmpty() || subscribedEventTypes.contains(event.getType())) {
        if (subscriber.getEventFilter() == null || subscriber.getEventFilter().apply(event)) {
          subscriber.receive(event);
        }
      }
    }
  }

}
