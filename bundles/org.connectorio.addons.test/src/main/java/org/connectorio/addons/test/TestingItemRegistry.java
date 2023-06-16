package org.connectorio.addons.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemNotUniqueException;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.ItemRegistry;

public class TestingItemRegistry implements ItemRegistry {

  private final Map<String, Item> items = new LinkedHashMap<>();

  public TestingItemRegistry(Item ... items) {
    for (Item item : items) {
      associate(item);
    }
  }

  private void associate(Item item) {
    this.items.put(item.getName(), item);

    // assign item to its group elements
    for (String group : item.getGroupNames()) {
      if (items.containsKey(group)) {
        Item parent = items.get(group);
        if (parent instanceof GroupItem) {
          ((GroupItem) parent).addMember(item);
        }
      }
    }

    for (Item existing : getAll()) {
      if (existing.getGroupNames().contains(item.getName()) && item instanceof GroupItem) {
        ((GroupItem) item).addMember(existing);
      }
    }
  }

  public TestingItemRegistry(ItemProvider provider) {
    for (Item item : provider.getAll()) {
      associate(item);
    }
  }

  @Override
  public Item getItem(String name) throws ItemNotFoundException {
    return Optional.ofNullable(items.get(name)).orElseThrow(() -> new IllegalArgumentException(name));
  }

  @Override
  public Item getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException {
    Collection<Item> matchedItems = getItems(name);

    if (matchedItems.isEmpty()) {
      throw new ItemNotFoundException(name);
    }
    if (matchedItems.size() > 1) {
      throw new ItemNotUniqueException(name, matchedItems);
    }

    return matchedItems.iterator().next();
  }

  @Override
  public Collection<Item> getItems() {
    return Collections.unmodifiableCollection(items.values());
  }

  @Override
  public Collection<Item> getItemsOfType(String type) {
    return Collections.unmodifiableCollection(
      items.values().stream().filter(new TypePredicate(type))
        .collect(Collectors.toList())
    );
  }

  @Override
  public Collection<Item> getItems(String pattern) {
    return filter(new PatternPredicate(pattern));
  }

  @Override
  public Collection<Item> getItemsByTag(String ... tags) {
    return filter(new TagsPredicate(tags));
  }

  @Override
  public Collection<Item> getItemsByTagAndType(String type, String... tags) {
    return filter(new TagsPredicate(tags).and(new TagsPredicate(tags)));
  }

  @Override
  public <T extends Item> Collection<T> getItemsByTag(Class<T> typeFilter, String... tags) {
    return filter(new InstanceOfPredicate(typeFilter).and(new TagsPredicate(tags)));
  }

  private <T extends Item> Collection<T> filter(Predicate<Item> predicate) {
    Collection<T> matchedItems = new ArrayList<>();
    for (Item item : getItems()) {
      if (predicate.test(item)) {
        matchedItems.add((T) item);
      }
    }
    return matchedItems;
  }

  @Override
  public Item remove(String itemName, boolean recursive) {
    throw new UnsupportedOperationException("Operation is not implemented");
  }

  @Override
  public Collection<Item> getAll() {
    return Collections.unmodifiableCollection(items.values());
  }

  @Override
  public Stream<Item> stream() {
    return getAll().stream();
  }

  @Override
  public Item get(String key) {
    return items.get(key);
  }

  @Override
  public Item add(Item element) {
    associate(element);
    return element;
  }

  @Override
  public Item update(Item element) {
    associate(element);
    return element;
  }

  @Override
  public Item remove(String key) {
    return items.remove(key);
  }

  @Override
  public void addRegistryChangeListener(RegistryChangeListener<Item> listener) {
  }

  @Override
  public void removeRegistryChangeListener(RegistryChangeListener<Item> listener) {
  }

  static class PatternPredicate implements Predicate<Item> {

    private final Pattern pattern;

    PatternPredicate(String pattern) {
      this.pattern = Pattern.compile(pattern.replace("?", ".?").replace("*", ".*?"));
    }


    @Override
    public boolean test(Item item) {
      return pattern.matcher(item.getName()).matches();
    }
  }

  static class TagsPredicate implements Predicate<Item> {

    private final List<String> tags;

    TagsPredicate(String ... tags) {
      this.tags = Arrays.asList(tags);
    }

    @Override
    public boolean test(Item item) {
      return item.getTags().containsAll(tags);
    }
  }

  static class TypePredicate implements Predicate<Item> {

    private final String type;

    TypePredicate(String type) {
      this.type = type;
    }

    @Override
    public boolean test(Item item) {
      return item.getType().equals(type);
    }
  }

  static class InstanceOfPredicate implements Predicate<Item> {

    private final Class<?> type;

    InstanceOfPredicate(Class<?> type) {
      this.type = type;
    }

    @Override
    public boolean test(Item item) {
      return type.isInstance(item);
    }
  }
}
