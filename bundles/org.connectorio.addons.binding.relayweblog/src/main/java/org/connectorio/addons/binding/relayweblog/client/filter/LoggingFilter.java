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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An universal request logger, independent of the JAX-RS implementation.
 */
public class LoggingFilter implements ClientRequestFilter, ClientResponseFilter {

  private final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
  private final AtomicLong counter = new AtomicLong();

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    requestContext.setProperty("id", counter.incrementAndGet());
    String headers = requestContext.getHeaders().entrySet().stream()
      .map(entry -> entry.getKey() + ": " + entry.getValue())
      .reduce("", (e1, e2) -> e1 + "\n" + e2);
    logger.debug("Request ({}): {} {}\n{}", counter.get(), requestContext.getMethod(), requestContext.getUri(), headers);
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    String headers = requestContext.getHeaders().entrySet().stream()
      .map(entry -> entry.getKey() + ": " + entry.getValue())
      .reduce("", (e1, e2) -> e1 + "\n" + e2);

    logger.debug("Response ({}): {}\n{}", requestContext.getProperty("id"), responseContext.getStatus(), headers);
  }

}
