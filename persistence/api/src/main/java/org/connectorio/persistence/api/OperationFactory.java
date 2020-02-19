package org.connectorio.persistence.api;

public interface OperationFactory {

  <T> Calculation<T> create(OperationKind<T> kind);

}
