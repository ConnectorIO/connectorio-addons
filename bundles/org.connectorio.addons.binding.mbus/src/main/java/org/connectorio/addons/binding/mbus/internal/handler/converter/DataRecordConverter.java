package org.connectorio.addons.binding.mbus.internal.handler.converter;

import java.time.ZonedDateTime;
import java.util.Date;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openmuc.jmbus.DataRecord;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = Converter.class)
public class DataRecordConverter implements Converter {

  private final TimeZoneProvider timeZoneProvider;

  @Activate
  public DataRecordConverter(@Reference TimeZoneProvider timeZoneProvider) {
    this.timeZoneProvider = timeZoneProvider;
  }

  @Override
  public State toState(DataRecord record) {
    switch (record.getDataValueType()) {
      case LONG:
      case DOUBLE:
      case BCD:
        return new DecimalType(record.getScaledDataValue());
      case DATE:
        return convertDate(record.getDataValue());
      case STRING:
      case NONE:
        return new StringType(record.getDataValue().toString());
    }
    return null;
  }

  private State convertDate(Object input) {
    if (input instanceof Date) {
      Date date = (Date) input;
      ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), timeZoneProvider.getTimeZone());
      return new DateTimeType(zonedDateTime);
    }

    return UnDefType.NULL;
  }
}
