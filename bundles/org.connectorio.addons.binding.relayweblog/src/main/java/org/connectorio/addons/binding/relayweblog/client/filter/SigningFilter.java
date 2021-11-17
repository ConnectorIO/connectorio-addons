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
package org.connectorio.addons.binding.relayweblog.client.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import org.connectorio.addons.binding.relayweblog.client.SigningContext;

/**
 * By default Weblog requires two or three additional parameters:
 * - X-MICRO-TIME: a timestamp of an request
 * - X-HMAC-HASH: a hmac-sha512 from uri and timestamp and optionally payload, the hash seed is based upon password hash or
 * password and session token.
 * - X-SESSION-TOKEN: session token, required for all requests but login
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class SigningFilter implements ClientRequestFilter {

  private final String baseUri;
  private final SigningContext signingContext;
  private final Supplier<Long> clock;

  private ObjectMapper objectMapper = new ObjectMapper();

  public SigningFilter(String baseUri, SigningContext signingContext, Supplier<Long> clock) {
    this.baseUri = baseUri;
    this.signingContext = signingContext;
    this.clock = clock;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    MultivaluedMap<String, Object> headers = requestContext.getHeaders();
    long microtime = clock.get() / 1000;

    String uri = requestContext.getUri().toString().replace(baseUri, "");
    if (uri.contains("?")) {
      uri = uri.substring(0, uri.indexOf("?"));
    }
    String payload = "";

    if (requestContext.getEntity() != null && "post".equalsIgnoreCase(requestContext.getMethod())) {
      payload = objectMapper.writeValueAsString(requestContext.getEntity());
    }

    try {
      headers.add("X-MICRO-TIME", microtime);
      if (payload.isEmpty()) {
        headers.add("X-HMAC-HASH", signingContext.getHash(uri, microtime));
      } else {
        headers.add("X-HMAC-HASH", signingContext.getHash(uri, microtime, payload));
      }
    } catch (Exception e) {
      throw new IOException("Could not calculate request hash", e);
    }

    if (signingContext.getSession() != null) {
      headers.add("X-SESSION-TOKEN", signingContext.getSession());
    }
  }
}