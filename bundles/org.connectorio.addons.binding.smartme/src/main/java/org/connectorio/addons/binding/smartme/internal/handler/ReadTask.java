/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.smartme.internal.handler;

import java.time.OffsetDateTime;
import java.util.function.Function;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import org.connectorio.addons.binding.smartme.v1.ApiException;
import org.connectorio.addons.binding.smartme.v1.client.DevicesApi;
import org.connectorio.addons.binding.smartme.v1.client.model.Device;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTask implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(ReadTask.class);
  private final DevicesApi devicesApi;
  private final String deviceId;
  private final Thing thing;
  private final ThingHandlerCallback callback;

  public ReadTask(DevicesApi devicesApi, String deviceId, Thing thing, ThingHandlerCallback callback) {
    this.devicesApi = devicesApi;
    this.deviceId = deviceId;
    this.thing = thing;
    this.callback = callback;
  }

  @Override
  public void run() {
    try {
      Device device = devicesApi.devicesGet_0(deviceId);

      Unit<Power> powerUnit = parseUnit(device.getActivePowerUnit(), Units.WATT);
      Unit<Energy> energyUnit = parseUnit(device.getCounterReadingUnit(), Units.KILOWATT_HOUR);
      update(device, value -> createPower(device.getActivePower(), powerUnit), new ChannelUID(thing.getUID(), "activePower"));
      update(device, value -> createPower(device.getActivePowerL1(), powerUnit), new ChannelUID(thing.getUID(), "activePowerL1"));
      update(device, value -> createPower(device.getActivePowerL2(), powerUnit), new ChannelUID(thing.getUID(), "activePowerL2"));
      update(device, value -> createPower(device.getActivePowerL3(), powerUnit), new ChannelUID(thing.getUID(), "activePowerL3"));
      update(device, value -> createEnergy(device.getCounterReading(), energyUnit), new ChannelUID(thing.getUID(), "counterReading"));
      update(device, value -> createEnergy(device.getCounterReadingT1(), energyUnit), new ChannelUID(thing.getUID(), "counterReadingT1"));
      update(device, value -> createEnergy(device.getCounterReadingT2(), energyUnit), new ChannelUID(thing.getUID(), "counterReadingT2"));
      update(device, value -> createEnergy(device.getCounterReadingT3(), energyUnit), new ChannelUID(thing.getUID(), "counterReadingT3"));
      update(device, value -> createEnergy(device.getCounterReadingT4(), energyUnit), new ChannelUID(thing.getUID(), "counterReadingT4"));
      update(device, value -> createEnergy(device.getCounterReadingImport(), energyUnit), new ChannelUID(thing.getUID(), "counterReadingImport"));
      update(device, value -> createEnergy(device.getCounterReadingExport(), energyUnit), new ChannelUID(thing.getUID(), "counterReadingExport"));
      update(device, value -> createVoltage(device.getVoltage()), new ChannelUID(thing.getUID(), "voltage"));
      update(device, value -> createVoltage(device.getVoltageL1()), new ChannelUID(thing.getUID(), "voltageL1"));
      update(device, value -> createVoltage(device.getVoltageL2()), new ChannelUID(thing.getUID(), "voltageL2"));
      update(device, value -> createVoltage(device.getVoltageL3()), new ChannelUID(thing.getUID(), "voltageL3"));
      update(device, value -> createAmperage(device.getCurrent()), new ChannelUID(thing.getUID(), "current"));
      update(device, value -> createAmperage(device.getCurrentL1()), new ChannelUID(thing.getUID(), "currentL1"));
      update(device, value -> createAmperage(device.getCurrentL2()), new ChannelUID(thing.getUID(), "currentL2"));
      update(device, value -> createAmperage(device.getCurrentL3()), new ChannelUID(thing.getUID(), "currentL3"));
      update(device, value -> createTemperature(device.getTemperature()), new ChannelUID(thing.getUID(), "temperature"));
      update(device, value -> createDatetime(device.getValueDate()), new ChannelUID(thing.getUID(), "valueDate"));
    } catch (ApiException e) {
      logger.warn("Failed to refresh data of device {}", deviceId, e);
    }
  }

  private <X extends Quantity<X>> Unit<X> parseUnit(String unit, Unit<X> fallback) {
    Unit parsed = UnitUtils.parseUnit(unit);
    if (parsed != null) {
      return (Unit<X>) parsed;
    }
    return fallback;
  }

  private void update(Device device, Function<Device, State> mapper, ChannelUID channel) {
    State state = mapper.apply(device);
    if (state == null) {
      state = UnDefType.NULL;
    }
    callback.stateUpdated(channel, state);
  }

  private State createDatetime(OffsetDateTime dateTime) {
    return new DateTimeType(dateTime.toZonedDateTime());
  }

  private State createTemperature(Double value) {
    return quantity(value, Units.KELVIN);
  }

  private State createAmperage(Double value) {
    return quantity(value, Units.AMPERE);
  }

  private State createVoltage(Double value) {
    return quantity(value, Units.VOLT);
  }

  private State createEnergy(Double value, Unit<Energy> unit) {
    return quantity(value, unit);
  }

  private State createPower(Double value, Unit<Power> unit) {
    return quantity(value, unit);
  }

  private QuantityType<?> quantity(Double value, Unit<?> unit) {
    if (value == null) {
      return null;
    }
    return QuantityType.valueOf(value, unit);
  }

}
