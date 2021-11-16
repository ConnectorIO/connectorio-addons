package org.connectorio.addons.binding.askoheat.client.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationDeserializer extends JsonDeserializer<Duration> {

  private static final Pattern PATTERN = Pattern.compile("(\\d+) ([a-zA-z]+)");

  @Override
  public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    return null;
  }

  public static void main(String[] args) {
    DurationDeserializer deserializer = new DurationDeserializer();
    Duration duration = deserializer.parse("0 day 11 hours 42 min.");

    System.out.println(duration);
  }

  private Duration parse(String text) {
    text = text.toLowerCase(Locale.ENGLISH);
    Matcher matcher = PATTERN.matcher(text);
    Instant instant = Instant.EPOCH;

    Duration duration = Duration.ZERO;

    while(matcher.find()){
      int count = Integer.parseInt(matcher.group(1));
      String type = matcher.group(2);
      if (type.startsWith("hour")) {
        duration = duration.plusHours(count);
      } else if (type.startsWith("day")) {
        duration = duration.plusDays(count);
      } else if (type.startsWith("week")) {
        instant = instant.plus(Period.ofWeeks(count));
      } else if (type.startsWith("month")) {
        instant = instant.plus(Period.ofMonths(count));
      } else if (type.startsWith("year")) {
        instant = instant.plus(Period.ofYears(count));
      }
    }

    return duration;
  }
}
