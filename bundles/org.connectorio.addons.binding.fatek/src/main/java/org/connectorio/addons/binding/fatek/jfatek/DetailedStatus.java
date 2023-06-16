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

public class DetailedStatus {

  private int stationNo;

  // frame data
  private byte status;
  private UnitType unitType;
  private Points points;
  private float version;
  private long ladderSize;
  private long diCount;
  private long doCount;
  private long aiCount;
  private long aoCount;
  private long mRelayCount;
  private long sRelayCount;
  private long lRelayCount;
  private long rRegisterCount;
  private long dRegisterCount;
  private long timerCount;
  private long counterCount;

  public void setStationNo(int stationNo) {
    this.stationNo = stationNo;
  }

  public int getStationNo() {
    return stationNo;
  }

  public void setStatusByte(byte statusByte) {
    this.status = statusByte;
  }

  public byte getStatusByte() {
    return status;
  }

  public void setUnitType(UnitType unitType) {
    this.unitType = unitType;
  }

  public UnitType getUnitType() {
    return unitType;
  }


  public void setPoints(Points points) {
    this.points = points;
  }

  public Points getPoints() {
    return points;
  }

  public void setVersion(float version) {
    this.version = version;
  }

  public float getVersion() {
    return version;
  }

  public void setLadderSize(long ladderSize) {
    this.ladderSize = ladderSize;
  }

  public long getLadderSize() {
    return ladderSize;
  }

  public void setDiCount(long diCount) {
    this.diCount = diCount;
  }

  public long getDiCount() {
    return diCount;
  }

  public void setDoCount(long doCount) {
    this.doCount = doCount;
  }

  public long getDoCount() {
    return doCount;
  }

  public void setAiCount(long aiCount) {
    this.aiCount = aiCount;
  }

  public long getAiCount() {
    return aiCount;
  }

  public void setAoCount(long aoCount) {
    this.aoCount = aoCount;
  }

  public long getAoCount() {
    return aoCount;
  }

  public void setmRelayCount(long mRelayCount) {
    this.mRelayCount = mRelayCount;
  }

  public long getmRelayCount() {
    return mRelayCount;
  }

  public void setsRelayCount(long sRelayCount) {
    this.sRelayCount = sRelayCount;
  }

  public long getsRelayCount() {
    return sRelayCount;
  }

  public void setlRelayCount(long lRelayCount) {
    this.lRelayCount = lRelayCount;
  }

  public long getlRelayCount() {
    return lRelayCount;
  }

  public void setrRegisterCount(long rRegisterCount) {
    this.rRegisterCount = rRegisterCount;
  }

  public long getrRegisterCount() {
    return rRegisterCount;
  }

  public void setdRegisterCount(long dRegisterCount) {
    this.dRegisterCount = dRegisterCount;
  }

  public long getdRegisterCount() {
    return dRegisterCount;
  }

  public void setTimerCount(long timerCount) {
    this.timerCount = timerCount;
  }

  public long getTimerCount() {
    return timerCount;
  }

  public void setCounterCount(long counterCount) {
    this.counterCount = counterCount;
  }

  public long getCounterCount() {
    return counterCount;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DetailedStatus[");
    sb.append("stationNo=").append(stationNo);
    sb.append(", status=").append(status);
    sb.append(", unitType=").append(unitType);
    sb.append(", points=").append(points);
    sb.append(", version=").append(version);
    sb.append(", ladderSize=").append(ladderSize);
    sb.append(", diCount=").append(diCount);
    sb.append(", doCount=").append(doCount);
    sb.append(", aiCount=").append(aiCount);
    sb.append(", aoCount=").append(aoCount);
    sb.append(", mRelayCount=").append(mRelayCount);
    sb.append(", sRelayCount=").append(sRelayCount);
    sb.append(", lRelayCount=").append(lRelayCount);
    sb.append(", rRegisterCount=").append(rRegisterCount);
    sb.append(", dRegisterCount=").append(dRegisterCount);
    sb.append(", timerCount=").append(timerCount);
    sb.append(", counterCount=").append(counterCount);
    sb.append(", statusByte=").append(getStatusByte());
    sb.append(']');
    return sb.toString();
  }
}
