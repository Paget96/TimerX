package com.arsvechkarev.timerx.format;

import static com.arsvechkarev.timerx.TimeUnits.HOURS;
import static com.arsvechkarev.timerx.TimeUnits.MINUTES;
import static com.arsvechkarev.timerx.TimeUnits.R_MILLISECONDS;
import static com.arsvechkarev.timerx.TimeUnits.SECONDS;
import static com.arsvechkarev.timerx.util.Constants.EMPTY_STRING;
import static com.arsvechkarev.timerx.util.Constants.Patterns.ESCAPED_HOURS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.ESCAPED_MINUTES;
import static com.arsvechkarev.timerx.util.Constants.Patterns.ESCAPED_REM_MILLIS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.ESCAPED_SECONDS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.PATTERN_HAS_HOURS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.PATTERN_HAS_MINUTES;
import static com.arsvechkarev.timerx.util.Constants.Patterns.PATTERN_HAS_REM_MILLIS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.PATTERN_HAS_SECONDS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.STR_HOURS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.STR_MINUTES;
import static com.arsvechkarev.timerx.util.Constants.Patterns.STR_REM_MILLIS;
import static com.arsvechkarev.timerx.util.Constants.Patterns.STR_SECONDS;
import static com.arsvechkarev.timerx.util.Constants.STR_ZERO;
import static com.arsvechkarev.timerx.util.Constants.TimeValues.MILLIS_IN_HOUR;
import static com.arsvechkarev.timerx.util.Constants.TimeValues.MILLIS_IN_MINUTE;
import static com.arsvechkarev.timerx.util.Constants.TimeValues.MILLIS_IN_SECOND;
import static com.arsvechkarev.timerx.util.Constants.TimeValues.MINUTES_IN_HOUR;
import static com.arsvechkarev.timerx.util.Constants.TimeValues.NONE;
import static com.arsvechkarev.timerx.util.Constants.TimeValues.SECONDS_IN_MINUTE;

import androidx.annotation.NonNull;
import com.arsvechkarev.timerx.TimeUnits;
import com.arsvechkarev.timerx.util.Constants.Patterns;

/**
 * Main class that formatting input milliseconds into string representation according to
 * parse format. Also returns optimized delay for handler in {@link
 * com.arsvechkarev.timerx.Timer} and {@link com.arsvechkarev.timerx.Stopwatch} (see
 * {@link #getOptimizedDelay()})
 *
 * @author Arseniy Svechkarev
 * @see Analyzer
 */
public class TimeFormatter {

  /**
   * Semantic representation of input format creates by {@link Analyzer}
   *
   * @see Semantic
   */
  private final Semantic semantic;

  /**
   * Temporary container for time units
   */
  private final TimeContainer timeContainer;

  /**
   * Input string format format
   */
  private final String format;

  public TimeFormatter(@NonNull Semantic semantic) {
    this.semantic = semantic;
    this.format = semantic.getFormat();
    timeContainer = new TimeContainer();
  }

  /**
   * Returns optimized delay for handler in Timer and Stopwatch. Optimized delay
   * calculates depending on which symbols contains in parse format. For example, if input
   * format contains {@link Patterns#STR_REM_MILLIS}, then delay for handler should be
   * every millisecond (on every ten milliseconds, depending on number of {@link
   * Patterns#STR_REM_MILLIS} symbols in format), but if input format do not contains
   * {@link Patterns#STR_REM_MILLIS}, there is no reason to notify user about changing
   * time every milliseconds, because millis will not displayed. But delay should be at
   * most 100 for correct pause/resume handling
   */
  public long getOptimizedDelay() {
    long delay = 100;
    if (semantic.has(R_MILLISECONDS)) {
      if (semantic.countOf(R_MILLISECONDS) == 2) {
        delay = 10;
      } else if (semantic.countOf(R_MILLISECONDS) > 2) {
        delay = 1;
      }
    }
    return delay;
  }

  /**
   * Returns minimum unit of semantic converted to millis
   *
   * @see com.arsvechkarev.timerx.Timer
   */
  public long minimumUnitInMillis() {
    if (semantic.minimumUnit() == R_MILLISECONDS) {
      return 1;
    } else if (semantic.minimumUnit() == SECONDS) {
      return MILLIS_IN_SECOND;
    } else if (semantic.minimumUnit() == MINUTES) {
      return MILLIS_IN_MINUTE;
    }
    return MILLIS_IN_HOUR;
  }

  public String currentFormat() {
    return semantic.getFormat();
  }

  /**
   * Actually, formats milliseconds to string representation according by {@link #format}
   *
   * @param time Time in milliseconds
   * @return Formatted time
   */
  @NonNull
  public String format(long time) {
    TimeContainer units = timeUnitsOf(time);
    long millisToShow = NONE;
    long secondsToShow = NONE;
    long minutesToShow = NONE;
    long hoursToShow = NONE;
    if (semantic.has(R_MILLISECONDS)) {
      millisToShow = (semantic.has(SECONDS)) ? units.remMillis : units.millis;
    }
    if (semantic.has(SECONDS)) {
      secondsToShow = (semantic.has(MINUTES)) ? units.remSeconds : units.seconds;
    }
    if (semantic.has(MINUTES)) {
      minutesToShow = (semantic.has(HOURS)) ? units.remMinutes : units.minutes;
    }
    if (semantic.has(HOURS)) {
      hoursToShow = units.hours;
    }
    return applyFormatOf(millisToShow, secondsToShow, minutesToShow, hoursToShow);
  }

  private TimeContainer timeUnitsOf(long millis) {
    long seconds = millis / MILLIS_IN_SECOND;
    long minutes = seconds / SECONDS_IN_MINUTE;
    long hours = minutes / MINUTES_IN_HOUR;
    long remMillis = millis % MILLIS_IN_SECOND;
    long remSeconds = seconds - minutes * SECONDS_IN_MINUTE;
    long remMinutes = minutes - hours * MINUTES_IN_HOUR;
    return timeContainer
        .setMillis(millis)
        .setSeconds(seconds)
        .setMinutes(minutes)
        .setHours(hours)
        .setRemMillis(remMillis)
        .setRemSeconds(remSeconds)
        .setRemMinutes(remMinutes);
  }

  private String applyFormatOf(long millisToShow, long secondsToShow,
      long minutesToShow, long hoursToShow) {
    String strHours = getFormatOf(hoursToShow, HOURS);
    String strMinutes = getFormatOf(minutesToShow, MINUTES);
    String strSeconds = getFormatOf(secondsToShow, SECONDS);
    String strMillis = getFormatOf(millisToShow, R_MILLISECONDS);

    return format
        .replaceAll(PATTERN_HAS_HOURS, strHours)
        .replaceAll(PATTERN_HAS_MINUTES, strMinutes)
        .replaceAll(PATTERN_HAS_SECONDS, strSeconds)
        .replaceAll(PATTERN_HAS_REM_MILLIS, strMillis)
        .replaceAll(ESCAPED_HOURS, STR_HOURS)
        .replaceAll(ESCAPED_MINUTES, STR_MINUTES)
        .replaceAll(ESCAPED_SECONDS, STR_SECONDS)
        .replaceAll(ESCAPED_REM_MILLIS, STR_REM_MILLIS);
  }

  private String getFormatOf(long number, TimeUnits numberType) {
    if (number != NONE) {
      if (number == 0) {
        return zerosBy(semantic.countOf(numberType));
      }
      int semanticCount = semantic.countOf(numberType);
      int numberLength = lengthOf(number);
      int diff = semanticCount - numberLength;
      if (numberType == R_MILLISECONDS && !semantic.hasOnlyRMillis()) {
        return formatRMillis(number, semanticCount);
      }
      if (diff > 0) {
        return zerosBy(diff) + number;
      }
      return number + EMPTY_STRING;
    }
    return EMPTY_STRING;
  }

  private String zerosBy(int num) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < num; i++) {
      builder.append(STR_ZERO);
    }
    return builder.toString();
  }

  private String formatRMillis(long millis, int semanticCount) {
    String strMillis = formatToThreeOrMoreDigits(millis, semanticCount);
    if (semanticCount <= 2) {
      return strMillis.substring(0, semanticCount);
    }
    return strMillis;
  }

  private String formatToThreeOrMoreDigits(long millis, int semanticCount) {
    int numLength = lengthOf(millis);
    StringBuilder result = new StringBuilder();
    if (numLength == 1) {
      result.append(STR_ZERO).append(STR_ZERO);
    }
    if (numLength == 2) {
      result.append(STR_ZERO);
    }
    int zerosToAdd = semanticCount - (result.length() + numLength);
    if (zerosToAdd > 0) {
      result.insert(0, zerosBy(zerosToAdd)).append(millis);
    } else {
      result.append(millis);
    }
    return result.toString();
  }

  private int lengthOf(long number) {
    return (number + EMPTY_STRING).length();
  }
}
