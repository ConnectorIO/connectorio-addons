package org.connectorio.persistence.api;

import java.time.Instant;

public interface TimedBoundOperation<T> extends Operation<T> {

  void setInstant(Instant instant);

}
