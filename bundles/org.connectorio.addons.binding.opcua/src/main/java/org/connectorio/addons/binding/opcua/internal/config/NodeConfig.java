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
package org.connectorio.addons.binding.opcua.internal.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.connectorio.addons.binding.config.Configuration;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.openhab.core.util.HexUtils;

public class NodeConfig implements Configuration {

  public int ns;
  public IdentifierType identifierType;
  public Integer identifier;
  public String stringIdentifier = "";
  public long publishInterval = 0L;

  public enum IdentifierType {
    i, s, g, b;
  }

  public NodeId createNodeId() {
    switch (this.identifierType) {
      case i:
        return new NodeId(this.ns, UInteger.valueOf(this.identifier));
      case s:
        return new NodeId(this.ns, this.stringIdentifier);
      case g:
        return new NodeId(this.ns, UUID.fromString(this.stringIdentifier));
      case b:
        return new NodeId(this.ns, ByteString.of(HexUtils.hexToBytes(this.stringIdentifier)));
    }
    return null;
  }

  public static Map<String, Object> createNodeConfig(NodeId nodeId) {
    Map<String, Object> config = new HashMap<>();
    config.put("ns", nodeId.getNamespaceIndex().intValue());
    switch (nodeId.getType()) {
      case Numeric:
        config.put("identifierType", IdentifierType.i.name());
        config.put("identifier", nodeId.getIdentifier());
        break;
      case String:
        config.put("identifierType", IdentifierType.s.name());
        config.put("stringIdentifier", nodeId.getIdentifier());
        break;
      case Guid:
        config.put("identifierType", IdentifierType.g.name());
        config.put("stringIdentifier", nodeId.getIdentifier());
        break;
      case Opaque:
        config.put("identifierType", IdentifierType.b.name());
        config.put("stringIdentifier", nodeId.getIdentifier());
        break;
    }

    return config;
  }

}
