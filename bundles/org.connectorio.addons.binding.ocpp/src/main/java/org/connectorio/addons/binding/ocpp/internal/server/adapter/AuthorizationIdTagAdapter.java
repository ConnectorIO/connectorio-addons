package org.connectorio.addons.binding.ocpp.internal.server.adapter;

import eu.chargetime.ocpp.model.core.AuthorizationStatus;
import eu.chargetime.ocpp.model.core.AuthorizeConfirmation;
import eu.chargetime.ocpp.model.core.AuthorizeRequest;
import eu.chargetime.ocpp.model.core.IdTagInfo;
import java.util.Set;
import java.util.UUID;

public class AuthorizationIdTagAdapter extends CoreEventHandlerAdapter {

  private final Set<String> tags;

  public AuthorizationIdTagAdapter(Set<String> tags) {
    this.tags = tags;
  }

  @Override
  public AuthorizeConfirmation handleAuthorizeRequest(UUID sessionIndex, AuthorizeRequest request) {
    String tag = request.getIdTag();

    if (tags.isEmpty() || tags.contains(tag)) {
      return new AuthorizeConfirmation(new IdTagInfo(AuthorizationStatus.Accepted));
    }

    return new AuthorizeConfirmation(new IdTagInfo(AuthorizationStatus.Invalid));
  }

}
