package org.connectorio.persistence.api;

import java.time.Instant;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.persistence.HistoricItem;

public interface OperationKind<T> {

  interface RetrievalKind extends OperationKind<HistoricItem> {
    RetrievalKind MINIMUM = new RetrievalKind() {};
    RetrievalKind MAXIMUM = new RetrievalKind() {};
    RetrievalKind PREVIOUS_STATE = new RetrievalKind() {};
  }

  interface CalculationKind<T> extends OperationKind<T> {
    CalculationKind<Boolean> CHANGED = new CalculationKind<Boolean>() {};
    CalculationKind<Boolean> UPDATED = new CalculationKind<Boolean>() {};

    CalculationKind<DecimalType> DELTA = new CalculationKind<DecimalType>() {};
    CalculationKind<DecimalType> AVERAGE = new CalculationKind<DecimalType>() {};
    CalculationKind<DecimalType> EVOLUTION = new CalculationKind<DecimalType>() {};
    CalculationKind<DecimalType> SUM = new CalculationKind<DecimalType>() {};

    CalculationKind<Instant> LAST_UPDATE = new CalculationKind<Instant>() {};
  }

  OperationKind<Iterable<HistoricItem>> SCAN = new OperationKind<Iterable<HistoricItem>>() {};

}
