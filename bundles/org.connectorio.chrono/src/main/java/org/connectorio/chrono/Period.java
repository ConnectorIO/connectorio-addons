package org.connectorio.chrono;

public enum Period {
  YEAR ("Every year"),
  MONTH ("Every month"),
  WEEK ("Every week"),
  DAY ("Every day"),
  HOUR ("Every hour"),
  HALF_HOUR ("Every half hour"),
  QUARTER_HOUR("Every quarter of an hour"),
  MINUTE ("Every minute"),
  SECOND ("Every second");

  private final String label;

  Period(String description) {
    this.label = description;
  }

  public String getLabel() {
    return label;
  }

}
