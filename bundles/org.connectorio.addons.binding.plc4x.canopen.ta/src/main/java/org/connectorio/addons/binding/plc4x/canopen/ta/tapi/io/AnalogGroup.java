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

import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;

public class AnalogGroup {

  private final int index;
  private final CANOpenService service;
  private final int node;

  public AnalogGroup(int node, int index) {
    this.index = index;
    this.service = calculateService(index);
    this.node = calculateNode(index, node);
  }

  public int getIndex() {
    return index;
  }

  public int getStartBoundary() {
    // boundary is tied to object index!
    return 1 + index - (index % 4);
  }

  public int getEndBoundary() {
    return getStartBoundary() + 4;
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

  private static CANOpenService service(int value) {
    for (CANOpenService service : CANOpenService.values()) {
      if (service.getMin() >= value && service.getMax() >= value) {
        System.out.println(value + "=" + service);
        return service;
      }
    }
    return null;
  }

  private static int calculateNode(int index, int nodeId) {
    if (index >= 17) {
      return 0x40 + nodeId;
    }

    return nodeId;
  }

}
