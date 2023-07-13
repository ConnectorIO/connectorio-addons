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
package org.connectorio.addons.binding.s7.internal.config;

import org.apache.plc4x.java.s7.readwrite.types.S7ControllerType;

public enum ControllerType {
    ANY (S7ControllerType.ANY),
    S7_300 (S7ControllerType.S7_300),
    S7_400 (S7ControllerType.S7_400),
    S7_1200 (S7ControllerType.S7_1200),
    S7_1500 (S7ControllerType.S7_1500),
    LOGO (S7ControllerType.LOGO);

    private final S7ControllerType type;

    ControllerType(S7ControllerType type) {
        this.type = type;
    }

    public S7ControllerType getType() {
        return type;
    }
}