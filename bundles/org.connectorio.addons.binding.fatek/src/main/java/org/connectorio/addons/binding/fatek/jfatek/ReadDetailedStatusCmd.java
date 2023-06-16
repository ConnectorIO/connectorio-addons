/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.fatek.jfatek;

import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekException;
import org.simplify4u.jfatek.FatekPLC;
import org.simplify4u.jfatek.io.FatekConnection;
import org.simplify4u.jfatek.io.FatekIOException;
import org.simplify4u.jfatek.io.FatekReader;
import org.simplify4u.jfatek.io.FatekWriter;

public class ReadDetailedStatusCmd extends FatekCommand<DetailedStatus> {

  public static final int CMD_ID = 0x53;

  private DetailedStatus status = new DetailedStatus();
  private int stationNo;

  public ReadDetailedStatusCmd(FatekPLC fatekPLC) {
    super(fatekPLC);
  }

  public int getID() {
    return CMD_ID;
  }

  protected void writeData(FatekWriter writer) throws FatekException, FatekIOException {
  }

  protected void readData(FatekReader reader) throws FatekIOException {
    status.setStationNo(stationNo);
    status.setStatusByte((byte) reader.readByte());
    status.setUnitType(UnitType.valueOf((byte) reader.readByte()));
    status.setPoints(Points.valueOf((byte) reader.readByte()));
    status.setVersion((float) (reader.readByte() / 10));
    status.setLadderSize(reader.readInt16());
    status.setDiCount(reader.readInt16());
    status.setDoCount(reader.readInt16());
    status.setAiCount(reader.readInt16());
    status.setAoCount(reader.readInt16());
    status.setmRelayCount(reader.readInt16());
    status.setsRelayCount(reader.readInt16());
    status.setlRelayCount(reader.readInt16());
    status.setrRegisterCount(reader.readInt16());
    status.setdRegisterCount(reader.readInt16());
    status.setTimerCount(reader.readInt16());
    status.setCounterCount(reader.readInt16());
  }

  public DetailedStatus getResult() {
    return status;
  }

  @Override
  protected void execute(FatekConnection conn) throws FatekIOException, FatekException {
    stationNo = conn.getPlcId();
    super.execute(conn);
  }
}
