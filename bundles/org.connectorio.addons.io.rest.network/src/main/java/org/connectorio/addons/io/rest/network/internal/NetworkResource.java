package org.connectorio.addons.io.rest.network.internal;

import java.util.Collections;
import java.util.List;
import org.connectorio.addons.network.Network;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;
import org.connectorio.addons.network.rest.v1.NetworksApi;

@Component(service = { RESTResource.class})
//@JaxrsResource
//@JaxrsName(NetworkResource.PATH_NETWORK)
//@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
//@JSONRequired
//@Path(NetworkResource.PATH_NETWORK)
//@RolesAllowed({ Role.ADMIN })
//@SecurityRequirement(name = "oauth2", scopes = { "admin" })
public class NetworkResource implements RESTResource, NetworksApi {


  public static final String PATH_NETWORK = "network";

  public List<org.connectorio.addons.network.rest.v1.model.Network> networks() {
    return Collections.emptyList();
  }

}
