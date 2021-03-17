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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io;

import static org.apache.plc4x.java.canopen.readwrite.types.CANOpenService.*;

import java.util.Objects;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;

/**
 * Analog inputs and outputs are packed up in several groups. Each having exactly 4 members.
 *
 * This class counts additional information which is needed to work with CAN level message exchanges. First of all it
 * picks up valid PDO address space (RPDO 1..2, TPDO 2..3) as well as node identifier which has 0x40 offset above 17th
 * element.
 */
public class AnalogGroup {

  private final CANOpenService service;
  private final int node;
  private int startBoundary;

  public AnalogGroup(int node, int index) {
    this.service = calculateService(index);
    this.node = calculateNode(index, node);
    // boundary is tied to object index!
    this.startBoundary = calculateStartBoundary(index);
  }

  public int getStartBoundary() {
    return startBoundary;
  }

  public int getEndBoundary() {
    return getStartBoundary() + 3;
  }

  public CANOpenService getService() {
    return service;
  }

  public int getNodeId() {
    return node;
  }

  public int getCobId() {
    return service.getMin() + node;
  }

  public String toString() {
    return "Analog Group [" + getStartBoundary() + "," + getEndBoundary() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AnalogGroup)) {
      return false;
    }
    AnalogGroup that = (AnalogGroup) o;
    return node == that.node && getService() == that.getService();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getService(), node);
  }

  private int calculateStartBoundary(int index) {
    if (index % 4 == 0) {
      return 1 + index - 4;
    }
    return 1 + index - (index % 4);
  }

  private static CANOpenService calculateService(int index) {
    if (index >= 1 && index <= 4) {
      return RECEIVE_PDO_1; //service(0x200);
    } else if (index >= 5 && index <= 8) {
      return TRANSMIT_PDO_2; //service(0x280);
    } else if (index >= 9 && index <= 12) {
      return RECEIVE_PDO_2; //service(0x300);
    } else if (index >= 13 && index <= 16) {
      return TRANSMIT_PDO_3; //service(0x380);
    } else if (index >= 17 && index <= 20) {
      return RECEIVE_PDO_1; //service(0x240);
    } else if (index >= 21 && index <= 24) {
      return TRANSMIT_PDO_2; //service(0x2C0);
    } else if (index >= 25 && index <= 28) {
      return RECEIVE_PDO_2; //service(0x340);
    } else if (index >= 29 && index <= 32) {
      return TRANSMIT_PDO_3; //service(0x3C0);
    }
    throw new IllegalStateException("Could not determine CANopen service for analog " + index);
  }

  private static int calculateNode(int index, int nodeId) {
    if (index >= 17) {
      return 0x40 + nodeId;
    }

    return nodeId;
  }

}
