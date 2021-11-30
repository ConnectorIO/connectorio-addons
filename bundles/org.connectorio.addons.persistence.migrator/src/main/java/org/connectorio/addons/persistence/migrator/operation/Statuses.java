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
package org.connectorio.addons.persistence.migrator.operation;

/**
 * Predefined statuses for migration execution outcomes.
 */
public interface Statuses {

  Status SUCCESS = new BasicStatus("success");
  Status FAILURE = new BasicStatus("failure");
  Status WAITING = new BasicStatus("waiting");
  Status SKIPPED = new BasicStatus("skipped");
  Status IGNORED = new BasicStatus("ignored");

  class BasicStatus implements Status {

    private final String code;

    public BasicStatus(String code) {
      this.code = code;
    }

    @Override
    public String getCode() {
      return code;
    }

    public String toString() {
      return code;
    }
  }
}
