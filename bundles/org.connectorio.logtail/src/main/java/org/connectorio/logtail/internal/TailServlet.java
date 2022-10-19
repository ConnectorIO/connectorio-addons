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
package org.connectorio.logtail.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Deque;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

@Component(immediate = true, property = "org.ops4j.pax.logging.appender.name=co7io-web")
public class TailServlet extends HttpServlet implements PaxAppender {

  private final HttpService httpService;
  private final Deque<PaxLoggingEvent> events = new LinkedList<>();

  @Activate
  public TailServlet(@Reference HttpService httpService) throws ServletException, NamespaceException {
    this.httpService = httpService;
    this.httpService.registerServlet("/logs", this, null, httpService.createDefaultHttpContext());
  }

  @Deactivate
  public void deactivate() {
    try {
      httpService.unregister("/logs");
    } catch (IllegalArgumentException e) {
      // happens if we already failed to start
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    PrintWriter writer = resp.getWriter();
    writer.println("<meta http-equiv=\"refresh\" content=\"5\">");
    writer.println("<pre>");

    int count = 0;
    for (PaxLoggingEvent event : events) {
      LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault());
      writer.println(dateTime + " | " + event.getLevel() + " | " + event.getLoggerName() + " | " + event.getRenderedMessage());
      if ((++count % 10) == 0) {
        writer.flush();
      }
    }

    writer.println("</pre>");
  }

  @Override
  public void doAppend(PaxLoggingEvent paxLoggingEvent) {
    events.addFirst(paxLoggingEvent);
    if (events.size() > 2000) {
      events.removeLast();
    }
  }

}
