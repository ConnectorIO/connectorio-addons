/*
 * Copyright (C) 2022-2022 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.servlet;

import static j2html.TagCreator.body;
import static j2html.TagCreator.button;
import static j2html.TagCreator.each;
import static j2html.TagCreator.filter;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.label;
import static j2html.TagCreator.option;
import static j2html.TagCreator.output;
import static j2html.TagCreator.select;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import j2html.rendering.IndentedHtml;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.connectorio.addons.binding.bacnet.ObjectHandler;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

@Component
public class ScheduleServlet extends HttpServlet {

  public static final String SERVLET_PATH = "/bacnet/schedule";
  private final HttpService httpService;
  private final ThingRegistry thingRegistry;

  @Activate
  public ScheduleServlet(@Reference HttpService httpService, @Reference ThingRegistry thingRegistry) {
    this.httpService = httpService;
    this.thingRegistry = thingRegistry;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    render(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    render(req, resp);

    String schedule = req.getParameter("schedule");
    if (schedule == null || schedule.trim().isEmpty()) {
      return;
    }

    Thing thing = thingRegistry.get(new ThingUID(schedule));
    if (thing == null) {
      resp.getWriter().write("Schedule not found");
      return;
    }

    if (!(thing.getHandler() instanceof ObjectHandler)) {
      resp.getWriter().write("Unsupported object handler");
      return;
    }

    ObjectHandler handler = (ObjectHandler) thing.getHandler();
    BacNetClient client = handler.getClient().orElse(null);;
    if (client == null) {
      resp.getWriter().write("Client not found");
      return;
    }

    List<Object> data = client.getObjectAttributeValues(handler.getObject(),
      Arrays.asList(
        PropertyIdentifier.scheduleDefault.toString(),
        PropertyIdentifier.exceptionSchedule.toString(),
        PropertyIdentifier.weeklySchedule.toString()
      ));


    StringBuilder renderer = new StringBuilder();
    body(
        table(
          tr(
            th("Name"), th("value")
          ),
          tr(
            td("Default"), td(data.get(0).toString())
          ),
          tr(
            td("Exception schedule"), td(data.get(1).toString())
          ),
          tr(
            td("Weekly schedule"), td(data.get(2).toString())
          )
        )
    ).render(renderer);
    resp.getWriter().write(renderer.toString());
  }

  private void render(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String schedule = request.getParameter("schedule");

    IndentedHtml<StringBuilder> renderer = IndentedHtml.inMemory();
    body(
      h1("BACnet schedule Management"),
      form(
        select(
          option("none").withValue("").condAttr(schedule == null || schedule.trim().isEmpty(), "selected", "selected"),
          each(filter(thingRegistry.getAll(), thing -> BACnetBindingConstants.SCHEDULE_THING_TYPE.equals(thing.getThingTypeUID())),
            thing ->
              option(thing.getUID() + " " + thing.getLabel()).withValue(thing.getUID().toString())
                .condAttr(thing.getUID().toString().equals(schedule), "selected", "selected")
          )
        ).withName("schedule"),
        button().withType("submit").withText("Select schedule")
      ).withMethod("post").withAction(SERVLET_PATH)
    ).render(renderer);

    response.getWriter().write(renderer.output().toString());
  }

  @Activate
  void activate() throws Exception {
    httpService.registerServlet(SERVLET_PATH, this, new Hashtable<>(), null);
  }

  @Deactivate
  void deactivate() {
    httpService.unregister(SERVLET_PATH);
  }

}
