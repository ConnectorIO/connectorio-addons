/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.canopen.ta.internal.provider;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TACanOutputObject;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import static org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit.*;
import static org.connectorio.addons.binding.canopen.ta.internal.config.DigitalUnit.*;
import static org.connectorio.addons.binding.canopen.ta.internal.TACANopenBindingConstants.*;

@Component(property = "ta=true")
public class TAChannelTypeProvider implements ChannelTypeProvider, ConfigDescriptionProvider {

  private final static List<ChannelTypeDef> entries = new ArrayList<>(Arrays.asList(
    new ChannelTypeDef(TA_ANALOG_TEMPERATURE_CHANNEL_TYPE, "Number:Temperature", CELSIUS, KELVIN),
    new ChannelTypeDef(TA_ANALOG_LENGTH_CHANNEL_TYPE, "Number:Length", METRE, KILO_METRE, MILLIMETER),
    new ChannelTypeDef(TA_ANALOG_VOLUME_CHANNEL_TYPE, "Number:Volume", LITRE, CUBIC_METRE),
    new ChannelTypeDef(TA_ANALOG_INTENSITY_CHANNEL_TYPE, "Number:Intensity", IRRADIANCE),
    new ChannelTypeDef(TA_ANALOG_TIME_CHANNEL_TYPE, "Number:Time", SECOND, MINUTE, HOUR, DAY),
    new ChannelTypeDef(TA_ANALOG_POWER_CHANNEL_TYPE, "Number:Power", KILOWATT, WATT),
    new ChannelTypeDef(TA_ANALOG_ENERGY_CHANNEL_TYPE, "Number:Energy", KILOWATT_HOUR, MEGAWATT_HOUR),
    new ChannelTypeDef(TA_ANALOG_ELECTRIC_POTENTIAL_CHANNEL_TYPE, "Number:ElectricPotential", VOLT),
    new ChannelTypeDef(TA_ANALOG_ELECTRIC_CURRENT_CHANNEL_TYPE, "Number:ElectricCurrent", MILLI_AMPERE, AMPERE),
    new ChannelTypeDef(TA_ANALOG_ELECTRIC_RESISTANCE_CHANNEL_TYPE, "Number:ElectricResistance", KILOOHM),
    new ChannelTypeDef(TA_ANALOG_SPEED_CHANNEL_TYPE, "Number:Speed", KILOMETRE_PER_HOUR, METRE_PER_SECOND, MILLIMETER_PER_MINUTE, MILLIMETER_PER_HOUR, MILLIMETER_PER_DAY),
    new ChannelTypeDef(TA_ANALOG_VOLUMETRIC_FLOW_RATE_CHANNEL_TYPE, "Number:VolumetricFlowRate", LITRE_PER_HOUR, LITRE_PER_MINUTE, LITER_PER_DAY, CUBICMETRE_PER_MINUTE, CUBICMETRE_PER_HOUR, CUBICMETRE_PER_DAY),
    new ChannelTypeDef(TA_ANALOG_PRESSURE_CHANNEL_TYPE, "Number:Pressure", BAR, MILLIBAR),
    new ChannelTypeDef(TA_ANALOG_FREQUENCY_CHANNEL_TYPE, "Number:Frequency", HERTZ),
    new ChannelTypeDef(TA_ANALOG_ANGLE_CHANNEL_TYPE, "Number:Angle", PHASE_SHIFT_DEGREE),
    new ChannelTypeDef(TA_ANALOG_PULSE_CHANNEL_TYPE, "Number:Dimensionless", LITRE_PER_IMPULSE, IMPULSE, KILOWATT_PER_IMPULSE, CUBICMETRE_PER_IMPULSE, MILLIMETRE_PER_IMPULSE, LITER_PER_IMPULSE),
    new ChannelTypeDef(TA_ANALOG_GENERIC_CHANNEL_TYPE, "Number:Dimensionless", DIMENSIONLESS, HUMIDITY, POWER_FACTOR),
    new ChannelTypeDef(TA_DIGITAL_SWITCH_CHANNEL_TYPE, "Switch", OFF_ON) {
      @Override
      public StateDescriptionFragment getStateDescriptionFragment() {
        return StateDescriptionFragmentBuilder.create().withPattern("%s").build();
      }
      @Override
      public Optional<ConfigDescriptionParameter> getFallback() {
        return Optional.of(createFallback(Type.BOOLEAN).build());
      }
    },
    new ChannelTypeDef(TA_DIGITAL_CONTACT_CHANNEL_TYPE, "Contact", CLOSE_OPEN) {
      @Override
      public StateDescriptionFragment getStateDescriptionFragment() {
        return StateDescriptionFragmentBuilder.create().withPattern("%s").build();
      }
      @Override
      public Optional<ConfigDescriptionParameter> getFallback() {
        return Optional.of(createFallback(Type.BOOLEAN).build());
      }
    },
    new ChannelTypeDef(TA_ANALOG_RAS_TEMPERATURE_CHANNEL_TYPE, "Number:Temperature", TEMPERATURE_REGULATOR),
    new ChannelTypeDef(TA_ANALOG_RAS_MODE_CHANNEL_TYPE, "Number", "Regulator Mode", TEMPERATURE_REGULATOR) {
      @Override
      public StateDescriptionFragment getStateDescriptionFragment() {
        return StateDescriptionFragmentBuilder.create()
          .withPattern("%s")
          .withOption(new StateOption("0", "AUTO"))
          .withOption(new StateOption("1", "NORMAL"))
          .withOption(new StateOption("2", "LOWERED"))
          .withOption(new StateOption("3", "STANDBY"))
          .withMinimum(BigDecimal.ZERO).withMaximum(BigDecimal.valueOf(3))
          .withStep(BigDecimal.ONE)
          .build();
      }

      @Override
      public Optional<ConfigDescriptionParameter> getFallback() {
        ConfigDescriptionParameter fallback = createFallback(Type.INTEGER).withOptions(Arrays.asList(
          new ParameterOption("0", "AUTO"),
          new ParameterOption("1", "NORMAL"),
          new ParameterOption("2", "LOWERED"),
          new ParameterOption("3", "STANDBY")
        )).build();
        return Optional.of(fallback);
      }
    },
    new ChannelTypeDef(TA_ANALOG_RAS_MODEL_SINGLE_CHANNEL_TYPE, "Number", "Regulator Mode", RAS_MODE) {
      @Override
      public StateDescriptionFragment getStateDescriptionFragment() {
        return StateDescriptionFragmentBuilder.create()
          .withPattern("%s")
          .withOption(new StateOption("0", "AUTO"))
          .withOption(new StateOption("1", "NORMAL"))
          .withOption(new StateOption("2", "LOWERED"))
          .withOption(new StateOption("3", "STANDBY"))
          .withMinimum(BigDecimal.ZERO).withMaximum(BigDecimal.valueOf(3))
          .withStep(BigDecimal.ONE)
          .build();
      }

      @Override
      public Optional<ConfigDescriptionParameter> getFallback() {
        ConfigDescriptionParameter fallback = createFallback(Type.INTEGER).withOptions(Arrays.asList(
          new ParameterOption("0", "AUTO"),
          new ParameterOption("1", "NORMAL"),
          new ParameterOption("2", "LOWERED"),
          new ParameterOption("3", "STANDBY")
        )).build();
        return Optional.of(fallback);
      }
    }
  ));

  private final Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<>();
  private final Map<URI, ConfigDescription> configDescriptions = new HashMap<>();

  @Activate
  public TAChannelTypeProvider() {
    for (ChannelTypeDef def : entries) {
      ConfigUID configUID = new ConfigUID(def.getChannelType());
      ConfigDescription configDescriptor = createChannelConfigDescriptor(configUID, def);
      configDescriptions.put(configDescriptor.getUID(), configDescriptor);

      StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder.state(def.getChannelType(), def.getLabel(), def.getItemType())
        .withDescription("Mapping of " + def.getLabel() + " values reported or received by controller")
        .withConfigDescriptionURI(configDescriptor.getUID())
        .withStateDescriptionFragment(def.getStateDescriptionFragment());
      channelTypes.put(def.getChannelType(), channelTypeBuilder.build());
    }
  }

  @Override
  public Collection<ChannelType> getChannelTypes(Locale locale) {
    return channelTypes.values();
  }

  @Override
  public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
    return channelTypes.get(channelTypeUID);
  }

  @Override
  public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
    return configDescriptions.values();
  }

  @Override
  public ConfigDescription getConfigDescription(URI uri, Locale locale) {
    return configDescriptions.get(uri);
  }

  private ConfigDescription createChannelConfigDescriptor(ConfigUID configUID, ChannelTypeDef def) {
    ConfigDescriptionBuilder builder = ConfigDescriptionBuilder.create(URI.create(configUID.getAsString()));
    ConfigDescriptionParameterBuilder parameter = ConfigDescriptionParameterBuilder.create("unit", Type.TEXT);

    List<ParameterOption> parameterOptions = def.getUnits().stream()
      .map(unit -> new ParameterOption(unit.name(), unit.name()))
      .collect(Collectors.toList());
    parameter.withOptions(parameterOptions)
      .withLabel("Unit")
      .withDescription("Unit used by controller for channel measure.")
      .withRequired(true)
      .withDefault(def.getUnits().get(0).name());
    builder.withParameter(parameter.build());

    ConfigDescriptionParameter readObjectIndex = ConfigDescriptionParameterBuilder.create("readObjectIndex", Type.INTEGER)
      .withLabel("Read object index")
      .withDescription("Index of CAN output used by controller to report values.")
      .build();
    ConfigDescriptionParameter writeObjectIndex = ConfigDescriptionParameterBuilder.create("writeObjectIndex", Type.INTEGER)
      .withLabel("Write object index")
      .withDescription("Index of CAN input used by controller to receive values.")
      .build();
    builder.withParameter(readObjectIndex);
    builder.withParameter(writeObjectIndex);
    def.getFallback().ifPresent(builder::withParameter);

    return builder.build();
  }

  public static List<Channel> forObject(ThingUID thing, TACanInputOutputObject<?> object, TAUnit unit, String name, Integer inputKey) {
    if (TEMPERATURE_REGULATOR.equals(unit)) {
      return Arrays.asList(
        create(thing, object, unit, name + " Mode", TA_ANALOG_RAS_MODE_CHANNEL_TYPE, "Number", inputKey),
        create(thing, object, unit, name + " Temperature", TA_ANALOG_RAS_TEMPERATURE_CHANNEL_TYPE, "Number:Temperature", inputKey)
      );
    }

    for (ChannelTypeDef def : entries) {
      if (def.getUnits().contains(unit)) {
        return Collections.singletonList(create(thing, object, unit, name, def.getChannelType(), def.getItemType(), inputKey));
      }
    }

    return Collections.emptyList();
  }

  private static Channel create(ThingUID thing, TACanInputOutputObject<?> object, TAUnit unit, String name, ChannelTypeUID channelType,
    String itemType, Integer inputKey) {
    Map<String, Object> configuration = new HashMap<>();
    configuration.put("unit", unit.name());

    String prefix = unit instanceof AnalogUnit ? "analog" : "digital";
    if (object instanceof TACanOutputObject) {
      prefix += "-output";
      configuration.put("readObjectIndex", object.getIndex());
      if (inputKey != -1) {
        configuration.put("writeObjectIndex", inputKey);
      }
    } else {
      prefix += "-input";
      configuration.put("writeObjectIndex", object.getIndex());
    }

    ChannelUID uid = new ChannelUID(thing, channelType.getId() + "#" + prefix + "_" + object.getIndex());
    ChannelBuilder channelBuilder = ChannelBuilder.create(uid, itemType)
      .withLabel(name)
      .withType(channelType)
      .withKind(ChannelKind.STATE)
      .withConfiguration(new Configuration(configuration));

    return channelBuilder.build();
  }

  static class ConfigUID extends UID {

    public ConfigUID(ChannelTypeUID channelType) {
      super(BINDING_ID, channelType.getId());
    }

    @Override
    protected int getMinimalNumberOfSegments() {
      return 2;
    }
  }
}
