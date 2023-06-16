package org.connectorio.addons.network;

import org.openhab.core.common.registry.Identifiable;

public interface Network extends Identifiable<NetworkUID> {

  NetworkType getType();

}
