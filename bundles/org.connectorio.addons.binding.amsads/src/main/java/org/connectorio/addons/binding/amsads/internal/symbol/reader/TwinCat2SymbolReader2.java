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
package org.connectorio.addons.binding.amsads.internal.symbol.reader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import org.apache.plc4x.java.ads.readwrite.AdsDataType;
import org.apache.plc4x.java.ads.readwrite.AdsDataTypeTableEntry;
import org.apache.plc4x.java.ads.readwrite.AdsReadRequest;
import org.apache.plc4x.java.ads.readwrite.AdsReadResponse;
import org.apache.plc4x.java.ads.readwrite.AdsSymbolTableEntry;
import org.apache.plc4x.java.ads.readwrite.AdsTableSizes;
import org.apache.plc4x.java.ads.readwrite.AmsPacket;
import org.apache.plc4x.java.ads.readwrite.AmsTCPPacket;
import org.apache.plc4x.java.ads.readwrite.ReservedIndexGroups;
import org.apache.plc4x.java.ads.readwrite.ReturnCode;
import org.apache.plc4x.java.ads.tag.DirectAdsTag;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.ReadBufferByteBased;
import org.apache.plc4x.java.spi.transaction.RequestTransactionManager;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReader;

public class TwinCat2SymbolReader2 extends ReflectiveReader implements SymbolReader {

  private final PlcConnection connection;
  public TwinCat2SymbolReader2(PlcConnection connection) {
    super(connection);
    this.connection = connection;

    // feed common types into driver type table
    for (AdsDataType type : AdsDataType.values()) {
      AdsDataTypeTableEntry entry = new AdsDataTypeTableEntry(0L, 0L, 0L, 0L,
        type.getNumBytes(), 0L, 0L, 0L, 0, 0, type.name(), type.name(), "",
        Collections.emptyList(), Collections.emptyList(), new byte[]{});
      dataTypeTable.put(type.name(), entry);
    }
  }

  @Override
  public void onChange(Consumer<Void> consumer) {

  }

  @Override
  public CompletableFuture<Set<SymbolEntry>> read() {
    CompletableFuture<AdsTableSizes> future = readSymbolTableSizes();

//    return future.thenApply(s -> {
//      System.out.println(s);
//      return Collections.emptySet();
//    });
    return future.thenCompose(this::readDataTypeTable)
      .thenCompose(this::readSymbolList);
  }

  private CompletableFuture<AdsTableSizes> readSymbolTableSizes() {
    CompletableFuture<AdsTableSizes> future = new CompletableFuture<>();
    PlcReadRequest.Builder readRequestBuilder = connection.readRequestBuilder();
    PlcReadRequest request = readRequestBuilder.addTag("symbolTableSizes", new DirectAdsTag(ReservedIndexGroups.ADSIGRP_SYMBOL_AND_DATA_TYPE_SIZES.getValue(), 0L, "SINT", 24)).build();

    request.execute().whenComplete((response, error) -> {
      if (error != null) {
        future.completeExceptionally(error);
        return ;
      }
      try {
        ByteBuffer buffer = toBuffer(response, "symbolTableSizes");
        future.complete(AdsTableSizes.staticParse(new ReadBufferByteBased(buffer.array())));
      } catch (ParseException e) {
        future.completeExceptionally(new CompletionException("Failed to parse symbol table sizes", e));
      }
    });
    return future;
  }

  private CompletableFuture<AdsTableSizes> readDataTypeTable(AdsTableSizes symbols) {
    CompletableFuture<AdsTableSizes> result = new CompletableFuture<>();
    // feed custom types into driver
    AmsPacket readDataTypeTableRequest = new AdsReadRequest(configuration.getTargetAmsNetId(), configuration.getTargetAmsPort(),
      configuration.getSourceAmsNetId(), configuration.getSourceAmsPort(), 0, getInvokeId(),
      ReservedIndexGroups.ADSIGRP_DATA_TYPE_TABLE_UPLOAD.getValue(), 0x00000000, symbols.getDataTypeLength());
    RequestTransactionManager.RequestTransaction readDataTypeTableTx = tm.startRequest();

    result.whenComplete((r, e) -> {
      readDataTypeTableTx.endRequest();
    });
    AmsTCPPacket amsReadTableTCPPacket = new AmsTCPPacket(readDataTypeTableRequest);
    readDataTypeTableTx.submit(() -> conversation.sendRequest(amsReadTableTCPPacket)
      .expectResponse(AmsTCPPacket.class, Duration.ofMillis(configuration.getTimeoutRequest()))
      .onTimeout(result::completeExceptionally)
      .onError((p, e) -> result.completeExceptionally(e))
      .check(responseAmsPacket -> responseAmsPacket.getUserdata().getInvokeId() == readDataTypeTableRequest.getInvokeId())
      .unwrap(response -> (AdsReadResponse) response.getUserdata())
      .handle(readDataTypeTableResponse -> {
        if (readDataTypeTableResponse.getResult() != ReturnCode.OK) {
          result.completeExceptionally(new PlcException("Could not retrieve data type table, ads server returned status: " + readDataTypeTableResponse.getResult()));
          return;
        }

        // Parse the result.
        ReadBuffer rb = new ReadBufferByteBased(readDataTypeTableResponse.getData());
        for (int i = 0; i < symbols.getDataTypeCount(); i++) {
          try {
            AdsDataTypeTableEntry adsDataTypeTableEntry = AdsDataTypeTableEntry.staticParse(rb);
            dataTypeTable.put(adsDataTypeTableEntry.getDataTypeName(), adsDataTypeTableEntry);
          } catch (ParseException e) {
            throw new RuntimeException(e);
          }
        }
        result.complete(symbols);
      })
    );
    return result;
  }

  private CompletableFuture<Set<SymbolEntry>> readSymbolList(AdsTableSizes symbols) {
    CompletableFuture<Set<SymbolEntry>> future = new CompletableFuture<>();
    PlcReadRequest request = connection.readRequestBuilder()
      .addTag("symbolDescription", new DirectAdsTag(ReservedIndexGroups.ADSIGRP_SYM_UPLOAD.getValue(), 0x0L, "SINT", (int) symbols.getSymbolLength()))
      .build();
    request.execute().whenComplete((response, error) -> {
      ByteBuffer buffer = toBuffer(response, "symbolDescription");

      Set<SymbolEntry> resolvedSymbols = new LinkedHashSet<>();
      ReadBufferByteBased buff = new ReadBufferByteBased(buffer.array());
      for (int index = 0; index < symbols.getSymbolCount(); index++) {
        try {
          AdsSymbolTableEntry symbol = AdsSymbolTableEntry.staticParse(buff);

          AdsDataType dataType = AdsDataType.enumForValue((byte) symbol.getDataType());
          resolvedSymbols.add(new SymbolEntry(
            dataType,
            symbol.getName(),
            symbol.getComment(),
            symbol.getGroup(),
            symbol.getOffset(),
            symbol.getFlagReadOnly()
          ));
        } catch (ParseException e) {
          future.completeExceptionally(e);
        }
      }
      future.complete(resolvedSymbols);
    });
    return future;
  }

  private static ByteBuffer toBuffer(PlcReadResponse rsp, String fieldName) {
    List<PlcValue> symbols = (List<PlcValue>) rsp.getObject(fieldName);
    ByteBuffer buffer = ByteBuffer.allocate(symbols.size()).order(ByteOrder.LITTLE_ENDIAN);

    for (PlcValue byteVal : symbols) {
      byte byteValue = byteVal.getByte();
      buffer.put(byteValue);
    }
    buffer.rewind();
    return buffer;
  }
}
