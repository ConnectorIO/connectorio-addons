package org.connectorio.addons.test;

import java.util.function.Supplier;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;

public class StubEvent<T extends Event> {

  private final Supplier<T> event;
  private final EventSubscriber[] subscribers;

  public StubEvent(Supplier<T> event, EventSubscriber ... subscribers) {
    this.event = event;
    this.subscribers = subscribers;
  }

  public void fire() {
    new SubscriberCaller().accept(event.get(), subscribers);
  }
}
