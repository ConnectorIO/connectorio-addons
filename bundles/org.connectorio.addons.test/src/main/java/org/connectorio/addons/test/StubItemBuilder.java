package org.connectorio.addons.test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import javax.measure.Quantity;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.internal.items.ItemBuilderFactoryImpl;
import org.openhab.core.items.GroupFunction;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;

public class StubItemBuilder<T extends Item> {

  protected final ItemBuilder builder;

  protected StubItemBuilder(String type, String name) {
    this(new StubUnitProvider(), type, name);
  }

  protected StubItemBuilder(UnitProvider unitProvider, String type, String name) {
    this(createBuilder(unitProvider, type, name));
  }

  protected StubItemBuilder(ItemBuilder builder) {
    this.builder = builder;
  }

  public static StubItemBuilder<CallItem> createCall(String name) {
    return new StubItemBuilder<>(CoreItemFactory.CALL, name);
  }

  public static StubItemBuilder<ColorItem> createColor(String name) {
    return new StubItemBuilder<>(CoreItemFactory.COLOR, name);
  }

  public static StubItemBuilder<ContactItem> createContact(String name) {
    return new StubItemBuilder<>(CoreItemFactory.CONTACT, name);
  }

  public static StubItemBuilder<DateTimeItem> createDateTime(String name) {
    return new StubItemBuilder<>(CoreItemFactory.DATETIME, name);
  }

  public static StubItemBuilder<DimmerItem> createDimmer(String name) {
    return new StubItemBuilder<>(CoreItemFactory.DIMMER, name);
  }

  public static StubItemBuilder<ImageItem> createImage(String name) {
    return new StubItemBuilder<>(CoreItemFactory.IMAGE, name);
  }

  public static StubItemBuilder<LocationItem> createLocation(String name) {
    return new StubItemBuilder<>(CoreItemFactory.LOCATION, name);
  }

  public static StubItemBuilder<NumberItem> createQuantity(UnitProvider unitProvider, Class<? extends Quantity<?>> quantity, String name) {
    return new StubItemBuilder<>(unitProvider, CoreItemFactory.NUMBER + ":" + quantity.getSimpleName(), name);
  }

  public static StubItemBuilder<NumberItem> createNumber(String name) {
    return new StubItemBuilder<>(CoreItemFactory.NUMBER, name);
  }

  public static StubItemBuilder<PlayerItem> createPlayer(String name) {
    return new StubItemBuilder<>(CoreItemFactory.PLAYER, name);
  }

  public static StubItemBuilder<RollershutterItem> createRollershutter(String name) {
    return new StubItemBuilder<>(CoreItemFactory.ROLLERSHUTTER, name);
  }

  public static StubItemBuilder<StringItem> createString(String name) {
    return new StubItemBuilder<>(CoreItemFactory.STRING, name);
  }

  public static StubItemBuilder<SwitchItem> createSwitch(String name) {
    return new StubItemBuilder<>(CoreItemFactory.SWITCH, name);
  }

  public static StubItemBuilder<GroupItem> createGroup(UnitProvider unitProvider, String name) {
    return createGroup(unitProvider, null, name, null);
  }

  public static StubItemBuilder<GroupItem> createGroup(UnitProvider unitProvider, String type, String name, GroupFunction function) {
    ItemBuilder builder = createBuilder(unitProvider, GroupItem.TYPE, name);
    builder.withGroupFunction(function);
    if (type != null) {
      // name is irrelevant, so we stub it
      builder.withBaseItem(createBuilder(unitProvider, type, "base_stub_" + name).build());
    }
    return new StubItemBuilder<>(builder);
  }

  public StubItemBuilder<T> member(String ... groups) {
    builder.withGroups(Arrays.asList(groups));
    return this;
  }

  public StubItemBuilder<T> tags(String ... tags) {
    builder.withTags(new LinkedHashSet<>(Arrays.asList(tags)));
    return this;
  }

  public StubItemBuilder<T> label(String label) {
    builder.withLabel(label);
    return this;
  }

  public T build() {
    return (T) builder.build();
  }

  private static ItemBuilder createBuilder(UnitProvider unitProvider, String type, String name) {
    return new ItemBuilderFactoryImpl(new CoreItemFactory(unitProvider)).newItemBuilder(type, name);
  }

}
