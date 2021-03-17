/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.UID;
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

import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit.*;
import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit.*;
import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants.*;

@Component(property = "ta=true")
public class TAChannelTypeProvider implements ChannelTypeProvider, ConfigDescriptionProvider {

  private final static List<ChannelTypeDef> entries = new ArrayList<>(Arrays.asList(
    new ChannelTypeDef(TA_ANALOG_TEMPERATURE_THING_TYPE, "Number:Temperature", CELSIUS, KELVIN),
    new ChannelTypeDef(TA_ANALOG_LENGTH_THING_TYPE, "Number:Length", METRE, KILO_METRE, MILLIMETER),
    new ChannelTypeDef(TA_ANALOG_VOLUME_THING_TYPE, "Number:Volume", LITRE, CUBIC_METRE),
    new ChannelTypeDef(TA_ANALOG_INTENSITY_THING_TYPE, "Number:Intensity", IRRADIANCE),
    new ChannelTypeDef(TA_ANALOG_TIME_THING_TYPE, "Number:Time", SECOND, MINUTE, HOUR, DAY),
    new ChannelTypeDef(TA_ANALOG_POWER_THING_TYPE, "Number:Power", KILOWATT),
    new ChannelTypeDef(TA_ANALOG_ENERGY_THING_TYPE, "Number:Energy", KILOWATT_HOUR, MEGAWATT_HOUR),
    new ChannelTypeDef(TA_ANALOG_ELECTRIC_POTENTIAL_THING_TYPE, "Number:ElectricPotential", VOLT),
    new ChannelTypeDef(TA_ANALOG_ELECTRIC_CURRENT_THING_TYPE, "Number:ElectricCurrent", MILLI_AMPERE),
    new ChannelTypeDef(TA_ANALOG_ELECTRIC_RESISTANCE_THING_TYPE, "Number:ElectricResistance", KILOOHM),
    new ChannelTypeDef(TA_ANALOG_SPEED_THING_TYPE, "Number:Speed", KILOMETRE_PER_HOUR, METRE_PER_SECOND, MILLIMETER_PER_MINUTE, MILLIMETER_PER_HOUR, MILLIMETER_PER_DAY),
    new ChannelTypeDef(TA_ANALOG_VOLUMETRIC_FLOW_RATE_THING_TYPE, "Number:VolumetricFlowRate", LITRE_PER_HOUR, LITRE_PER_MINUTE, LITER_PER_DAY, CUBICMETRE_PER_MINUTE, CUBICMETRE_PER_HOUR, CUBICMETRE_PER_DAY),
    new ChannelTypeDef(TA_ANALOG_PRESSURE_THING_TYPE, "Number:Pressure", BAR, MEGABAR),
    new ChannelTypeDef(TA_ANALOG_FREQUENCY_THING_TYPE, "Number:Frequency", HERTZ),
    new ChannelTypeDef(TA_ANALOG_PULSE_THING_TYPE, "Number:Dimensionless", LITRE_PER_IMPULSE, IMPULSE, KILOWATT_PER_IMPULSE, CUBICMETRE_PER_IMPULSE, MILLIMETRE_PER_IMPULSE, LITER_PER_IMPULSE),
    new ChannelTypeDef(TA_ANALOG_GENERIC_THING_TYPE, "Number:Dimensionless", DIMENSIONLESS, HUMIDITY),
    new ChannelTypeDef(TA_DIGITAL_SWITCH_THING_TYPE, "Switch", OPEN_CLOSED, ON_OFF) {
      @Override
      public StateDescriptionFragment getStateDescriptionFragment() {
        return StateDescriptionFragmentBuilder.create().withPattern("%s").build();
      }
    },
    new ChannelTypeDef(TA_ANALOG_RAS_THING_TYPE, new ChannelTypeUID(TACANopenBindingConstants.BINDING_ID, TA_ANALOG_RAS), "Number", "Mode", TEMPERATURE_REGULATOR) {
      @Override
      public StateDescriptionFragment getStateDescriptionFragment() {
        return StateDescriptionFragmentBuilder.create()
          .withOption(new StateOption("0", "AUTO"))
          .withOption(new StateOption("1", "Mode #1"))
          .withOption(new StateOption("2", "Mode #2"))
          .withOption(new StateOption("3", "FROST"))
          .build();
      }
    }
  ));

  private final Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<>();
  private final Map<URI, ConfigDescription> configDescriptions = new HashMap<>();

  @Activate
  public TAChannelTypeProvider() {
    for (ChannelTypeDef def : entries) {
      ConfigUID configUID = new ConfigUID(def.getThingType().getId());
      ConfigDescription configDescriptor = createChannelConfigDescriptor(configUID, def);
      configDescriptions.put(configDescriptor.getUID(), configDescriptor);

      StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder.state(def.getChannelType(), def.getLabel(), def.getItemType());
      channelTypeBuilder.withConfigDescriptionURI(configDescriptor.getUID());
      channelTypeBuilder.withStateDescriptionFragment(def.getStateDescriptionFragment());
      ChannelType channelType = channelTypeBuilder.build();
      channelTypes.put(channelType.getUID(), channelType);
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
    return builder.build();
  }

  public static ThingTypeUID forUnit(TAUnit unit) {
    for (ChannelTypeDef def : entries) {
      if (def.getUnits().contains(unit)) {
        return def.getThingType();
      }
    }

    return null;
  }

  static class ConfigUID extends UID {

    public ConfigUID(String dimension) {
      super(TA_CONFIG_URI, "ta-channel-" + dimension);
    }

    @Override
    protected int getMinimalNumberOfSegments() {
      return 2;
    }
  }
}
