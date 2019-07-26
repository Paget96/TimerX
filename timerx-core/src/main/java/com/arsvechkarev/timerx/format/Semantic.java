package com.arsvechkarev.timerx.format;

import com.arsvechkarev.timerx.TimeUnits;

public class Semantic {

  private String format;

  private int hoursCount = 0;
  private int minutesCount = 0;
  private int secondsCount = 0;
  private int millisCount = 0;

  Semantic(String format) {
    this.format = format;
  }

  void setHoursCount(int hoursCount) {
    this.hoursCount = hoursCount;
  }

  void setMinutesCount(int minutesCount) {
    this.minutesCount = minutesCount;
  }

  void setSecondsCount(int secondsCount) {
    this.secondsCount = secondsCount;
  }

  void setMillisCount(int millisCount) {
    this.millisCount = millisCount;
  }

  public String getFormat() {
    return format;
  }

  boolean has(TimeUnits unitType) {
    switch (unitType) {
      case HOURS:
        return hoursCount > 0;
      case MINUTES:
        return minutesCount > 0;
      case SECONDS:
        return secondsCount > 0;
      case MILLISECONDS:
        return millisCount > 0;
      default:
        throw new IllegalArgumentException("Incorrect type of unit");
    }
  }

  int countOf(TimeUnits unitType) {
    switch (unitType) {
      case HOURS:
        return hoursCount;
      case MINUTES:
        return minutesCount;
      case SECONDS:
        return secondsCount;
      case MILLISECONDS:
        return millisCount;
      default:
        throw new IllegalArgumentException("Incorrect type of unit");
    }
  }

}
