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
package org.connectorio.addons.persistence.migrator.internal.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.connectorio.addons.persistence.migrator.internal.operation.CopyOperation;
import org.connectorio.addons.persistence.migrator.internal.operation.CopyPatternOperation;
import org.connectorio.addons.persistence.migrator.internal.operation.DeleteOperation;
import org.connectorio.addons.persistence.migrator.internal.operation.TruncateOperation;
import org.connectorio.addons.persistence.migrator.operation.Container;
import org.junit.jupiter.api.Test;

class MigrationXmlReaderTest {

  @Test
  void testReader() throws Exception {
    MigrationXmlReader reader = new MigrationXmlReader();
    Container migrations = reader.readFromXML(getClass().getResource("/1_migration.xml"));

    Migrations defined = new Migrations();
    defined.setService("jdbc");
    defined.setSteps(Arrays.asList(
      new CopyOperation("ITEM_A", null, "ITEM_B", "influx"),
      new DeleteOperation("ITEM_A", null),
      new TruncateOperation("ITEM_A", null),
      new CopyPatternOperation("ITEM_(.*?)", null, "ITEM_1_$1", "influx"),
      new CopyPatternOperation("ITEM_(\\w+)_(\\w+)", null, "ITEM_$2_$1", "influx")
    ));

    assertThat(defined).isEqualTo(migrations);
  }

}