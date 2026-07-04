/*
 * ChargeTime.eu - Java-OCA-OCPP
 *
 * MIT License
 *
 * Copyright (C) 2016-2018 Thomas Volden <tv@chargetime.eu>
 * Copyright (C) 2022 Emil Melar <emil@iconsultable.no>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package eu.chargetime.ocpp.model.core;

import eu.chargetime.ocpp.model.Request;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Sent by the Charge Point to the Central System — end of a transaction.
 *
 * <p>LOCAL OVERRIDE of the class embedded in {@code eu.chargetime.ocpp:v1_6} (this bundle's own
 * classes precede the embedded jar on the Bundle-ClassPath). The upstream {@link #validate()}
 * requires {@code meterStop}, {@code timestamp} and {@code transactionId} to all be present and
 * every {@code transactionData} entry to validate. A meter-less charge point running a local
 * (FreeMode) transaction — Phoenix Contact CHARX with no energy meter — can legitimately omit
 * {@code meterStop} or the transaction id, and pads {@code transactionData} with placeholder
 * samples. Upstream validation then makes the library answer with a CallError
 * (OccurenceConstraintViolation) instead of a StopTransaction confirmation; the charge point
 * retries, gives up (TransactionMessageAttempts) and keeps the transaction open — leaving a
 * phantom transaction that wedges the connector. OCPP 1.6 requires the CSMS to acknowledge
 * StopTransaction regardless, so this override only insists on a {@code timestamp} and leaves
 * interpretation of the remaining fields to the handler (which tolerates unknown/absent
 * transaction ids already).
 */
public class StopTransactionRequest implements Request {

  private String idTag;
  private Integer meterStop;
  private ZonedDateTime timestamp;
  private Integer transactionId;
  private Reason reason;
  private MeterValue[] transactionData;

  public StopTransactionRequest() {}

  public StopTransactionRequest(Integer meterStop, ZonedDateTime timestamp, Integer transactionId) {
    this.meterStop = meterStop;
    this.timestamp = timestamp;
    this.transactionId = transactionId;
  }

  @Override
  public boolean validate() {
    // Tolerant on purpose — see the class javadoc. A stop without meterStop/transactionId is
    // still a stop the CSMS must confirm; rejecting it strands the transaction on the charger.
    return timestamp != null;
  }

  public String getIdTag() {
    return idTag;
  }

  public void setIdTag(String idTag) {
    this.idTag = idTag;
  }

  public Integer getMeterStop() {
    return meterStop;
  }

  public void setMeterStop(Integer meterStop) {
    this.meterStop = meterStop;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(ZonedDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public ZonedDateTime objTimestamp() {
    return timestamp;
  }

  public Integer getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(Integer transactionId) {
    this.transactionId = transactionId;
  }

  public Reason getReason() {
    return reason;
  }

  public void setReason(Reason reason) {
    this.reason = reason;
  }

  public Reason objReason() {
    return reason;
  }

  public MeterValue[] getTransactionData() {
    return transactionData;
  }

  public void setTransactionData(MeterValue[] transactionData) {
    this.transactionData = transactionData;
  }

  @Override
  public boolean transactionRelated() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StopTransactionRequest that = (StopTransactionRequest) o;
    return Objects.equals(idTag, that.idTag)
        && Objects.equals(meterStop, that.meterStop)
        && Objects.equals(timestamp, that.timestamp)
        && Objects.equals(transactionId, that.transactionId)
        && reason == that.reason
        && Objects.deepEquals(transactionData, that.transactionData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idTag, meterStop, timestamp, transactionId, reason, transactionData);
  }

  @Override
  public String toString() {
    return "StopTransactionRequest{"
        + "idTag='" + idTag + '\''
        + ", meterStop=" + meterStop
        + ", timestamp=" + timestamp
        + ", transactionId=" + transactionId
        + ", reason=" + reason
        + ", transactionData=" + (transactionData == null ? "null" : transactionData.length + " entries")
        + '}';
  }
}
