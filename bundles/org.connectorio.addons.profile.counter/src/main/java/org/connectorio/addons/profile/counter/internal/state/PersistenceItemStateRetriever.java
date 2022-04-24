/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.profile.counter.internal.state;

import java.lang.reflect.Field;
import java.util.Iterator;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PersistenceItemStateRetriever implements LinkedItemStateRetriever {

  private final Logger logger = LoggerFactory.getLogger(PersistenceItemStateRetriever.class);
  private final PersistenceServiceRegistry persistenceServiceRegistry;

  @Activate
  public PersistenceItemStateRetriever(@Reference PersistenceServiceRegistry persistenceServiceRegistry) {
    this.persistenceServiceRegistry = persistenceServiceRegistry;
  }

  @Override
  public String getItemName(ProfileCallback callback) {
    Class<?> clazz = callback.getClass();

    Field linkField = null;
    do {
      try {
        linkField = clazz.getDeclaredField("link");
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    } while (linkField == null && clazz != Object.class);

    if (linkField == null) {
      return null;
    }

    if (ItemChannelLink.class.equals(linkField.getType())) {
      try {
        linkField.setAccessible(true);
        ItemChannelLink link = (ItemChannelLink) linkField.get(callback);
        if (link != null && link.getItemName() != null) {
          return link.getItemName();
        }
      } catch (IllegalAccessException e) {
        logger.warn("Could not extract link information from profile callback {}.", callback, e);
      }
    }
    return null;
  }

  @Override
  public State retrieveState(String itemName) {
    PersistenceService service = persistenceServiceRegistry.getDefault();
    if (service == null) {
      logger.error("Could not find default persistence service to retrieve last state of the counter!");
      return null;
    }
    if (!(service instanceof QueryablePersistenceService)) {
      logger.error("Default persistence service is not queryable!");
      return null;
    }
    Type type = retrieveState((QueryablePersistenceService) service, itemName);
    if (type instanceof State) {
      return (State) type;
    }
    logger.error("Last historical recording of linked item is not a state!");
    return null;
  }

  private Type retrieveState(QueryablePersistenceService service, String itemName) {
    FilterCriteria filter = new FilterCriteria().setItemName(itemName).setPageSize(1);
    Iterator<HistoricItem> results = service.query(filter).iterator();
    if (results.hasNext()) {
      HistoricItem result = results.next();
      return result.getState();
    }
    return null;
  }

}
