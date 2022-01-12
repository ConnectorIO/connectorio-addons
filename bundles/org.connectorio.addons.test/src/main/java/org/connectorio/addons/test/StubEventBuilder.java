package org.connectorio.addons.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.events.system.StartlevelEvent;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.items.events.ItemStatePredictedEvent;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.events.ChannelTriggeredEvent;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.openhab.core.thing.events.ThingStatusInfoEvent;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class StubEventBuilder<T extends Event> {

  static Argument<String> TOPIC = Argument.of("topic", String.class, "");
  static Argument<String> PAYLOAD = Argument.of("payload", String.class, "{}");
  static Argument<String> SOURCE = Argument.of("source", String.class, null);

  private final Supplier<T> event;

  protected StubEventBuilder(Supplier<T> event) {
    this.event = event;
  }

  public StubEvent<T> build(EventSubscriber ... subscribers) {
    return new StubEvent<T>(event, new CompositeSubscriber(subscribers));
  }

  public static StubEventBuilder<ItemCommandEvent> createItemCommandEvent(Item item, Command command) {
    return new StubEventBuilder<>(new EventCreator<>(ItemCommandEvent.class, TOPIC, PAYLOAD,
      Argument.of("itemName", String.class, item.getName()),
      Argument.of("command", Command.class, command),
      SOURCE
    ));
  }

  public static StubEventBuilder<ItemStateEvent> createItemStateEvent(Item item, State itemState) {
    return new StubEventBuilder<>(new EventCreator<>(ItemStateEvent.class, TOPIC, PAYLOAD,
      Argument.of("itemName", String.class, item.getName()),
      Argument.of("itemState", State.class, itemState),
      SOURCE
    ));
  }

  public static StubEventBuilder<ItemStateChangedEvent> createItemStateChangedEvent(Item item, State newItemState, State oldItemState) {
    return new StubEventBuilder<>(new EventCreator<>(ItemStateChangedEvent.class, TOPIC, PAYLOAD,
      Argument.of("itemName", String.class, item.getName()),
      Argument.of("newItemState", State.class, newItemState),
      Argument.of("oldItemState", State.class, oldItemState)
    ));
  }

  public static StubEventBuilder<ItemStatePredictedEvent> createItemStatePredictedEvent(Item item, State predictedState, boolean isConfirmation) {
    return new StubEventBuilder<>(new EventCreator<>(ItemStatePredictedEvent.class, TOPIC, PAYLOAD,
      Argument.of("itemName", String.class, item.getName()),
      Argument.of("predictedState", State.class, predictedState),
      Argument.of("isConfirmation", boolean.class, isConfirmation),
      SOURCE
    ));
  }

  public static StubEventBuilder<ThingStatusInfoEvent> createThingStatusInfoEvent(ThingUID thingUID, ThingStatusInfo thingStatusInfo) {
    return new StubEventBuilder<>(new EventCreator<>(ThingStatusInfoEvent.class, TOPIC, PAYLOAD,
      Argument.of("thingUID", ThingUID.class, thingUID),
      Argument.of("thingStatusInfo", ThingStatusInfo.class, thingStatusInfo),
      SOURCE
    ));
  }

  public static StubEventBuilder<ThingStatusInfoChangedEvent> createThingStatusInfoChangedEvent(ThingUID thingUID, ThingStatusInfo newThingStatusInfo, ThingStatusInfo oldThingStatusInfo) {
    return new StubEventBuilder<>(new EventCreator<>(ThingStatusInfoChangedEvent.class, TOPIC, PAYLOAD,
      Argument.of("thingUID", ThingUID.class, thingUID),
      Argument.of("newThingStatusInfo", ThingStatusInfo.class, newThingStatusInfo),
      Argument.of("oldThingStatusInfo", ThingStatusInfo.class, oldThingStatusInfo)
    ));
  }

  public static StubEventBuilder<StartlevelEvent> createStartLevelEvent(int startLevel) {
    return new StubEventBuilder<>(new EventCreator<>(StartlevelEvent.class, TOPIC, PAYLOAD,
      SOURCE,
      Argument.of("startLevel", Integer.class, startLevel)
    ));
  }

  public static StubEventBuilder<ChannelTriggeredEvent> createChannelTriggeredEvent(String event, ChannelUID channel) {
    return new StubEventBuilder<>(new EventCreator<>(ChannelTriggeredEvent.class, TOPIC, PAYLOAD,
      SOURCE,
      Argument.of("event", String.class, event),
      Argument.of("channel", ChannelUID.class, channel)
    ));
  }

  static class CompositeSubscriber implements EventSubscriber {

    private final EventSubscriber[] subscribers;

    CompositeSubscriber(EventSubscriber ... subscribers) {
      this.subscribers = subscribers;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
      return Collections.emptySet(); // we mock this anyway
    }

    @Override
    public EventFilter getEventFilter() {
      return null;
    }

    @Override
    public void receive(Event event) {
      new SubscriberCaller().accept(event, subscribers);
    }
  }

  static class EventCreator<T extends Event> implements Supplier<T> {

    private final Constructor<T> constructor;
    private final List<Argument> arguments;

    public EventCreator(Class<T> type, Argument<?> ... arguments) {
      this.arguments = Arrays.asList(arguments);
      try {
        Class<?>[] parameterTypes = (Class<?>[]) this.arguments.stream()
          .map(arg -> arg.type)
          .toArray(Class[]::new);
        constructor = type.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Could not find constructor", e);
      }
    }

    @Override
    public T get() {
      try {
        return constructor.newInstance(this.arguments.stream()
          .map(arg -> arg.value)
          .toArray()
        );
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException("Could not create event", e);
      }
    }
  }

  static class Argument<T> {
    final String name;
    final Class<T> type;
    final T value;

    Argument(String name, Class<T> type, T value) {
      this.name = name;
      this.type = type;
      this.value = value;
    }

    static <X> Argument<X> of(String name, Class<X> type, X value) {
      return new Argument<>(name, type, value);
    }
  }

}
