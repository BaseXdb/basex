package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for dates and durations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class ADateDur extends Item {
  /** Maximum long value. */
  public static final BigDecimal BDMAXLONG = BigDecimal.valueOf(Long.MAX_VALUE);
  /** Seconds per day. */
  static final BigDecimal DAYSECONDS = BigDecimal.valueOf(86400);
  /** BigDecimal: 146097. */
  static final BigDecimal BD146097 = BigDecimal.valueOf(146097);
  /** BigDecimal: 36525. */
  static final BigDecimal BD36525 = BigDecimal.valueOf(36525);
  /** BigDecimal: 36524. */
  static final BigDecimal BD36524 = BigDecimal.valueOf(36524);
  /** BigDecimal: 60. */
  static final BigDecimal BD3600 = BigDecimal.valueOf(3600);
  /** BigDecimal: 1461. */
  static final BigDecimal BD1461 = BigDecimal.valueOf(1461);
  /** BigDecimal: 1000. */
  static final BigDecimal BD1000 = BigDecimal.valueOf(1000);
  /** BigDecimal: 366. */
  static final BigDecimal BD366 = BigDecimal.valueOf(366);
  /** BigDecimal: 365. */
  static final BigDecimal BD365 = BigDecimal.valueOf(365);
  /** BigDecimal: 153. */
  static final BigDecimal BD153 = BigDecimal.valueOf(153);
  /** BigDecimal: 100. */
  static final BigDecimal BD100 = BigDecimal.valueOf(100);
  /** BigDecimal: 60. */
  static final BigDecimal BD60 = BigDecimal.valueOf(60);
  /** BigDecimal: 5. */
  static final BigDecimal BD5 = BigDecimal.valueOf(5);
  /** BigDecimal: 4. */
  static final BigDecimal BD4 = BigDecimal.valueOf(4);
  /** BigDecimal: 2. */
  static final BigDecimal BD2 = BigDecimal.valueOf(2);

  /** Seconds and milliseconds ({@code 0-59.\d+}). {@code -1}: undefined. */
  public BigDecimal sec;

  /**
   * Constructor.
   * @param t data type
   */
  ADateDur(final Type t) {
    super(t);
  }

  /**
   * Returns the years.
   * @return year
   */
  public abstract long yea();

  /**
   * Returns the months.
   * @return year
   */
  public abstract long mon();

  /**
   * Returns the days.
   * @return day
   */
  public abstract long day();

  /**
   * Returns the hours (0-23).
   * @return day
   */
  public abstract long hou();

  /**
   * Returns the minutes (0-59).
   * @return day
   */
  public abstract long min();

  /**
   * Returns the seconds (0-59), including the fractional part.
   * @return day
   */
  public abstract BigDecimal sec();

  /**
   * Throws a date format exception.
   * @param i input
   * @param ex example format
   * @param ii input info
   * @return date format exception
   */
  final QueryException dateError(final byte[] i, final String ex, final InputInfo ii) {
    return DATEFORMAT.get(ii, type, i, ex);
  }

  /**
   * Date and durations: converts the specified string to an integer value.
   * Returns an exception if the value is invalid.
   * @param s string to be converted
   * @param dur duration
   * @param ii input info
   * @return long value
   * @throws QueryException query exception
   */
  long toLong(final String s, final boolean dur, final InputInfo ii) throws QueryException {
    try {
      return Long.parseLong(s);
    } catch(final NumberFormatException ex) {
      throw (dur ? DURRANGE : DATERANGE).get(ii, type, chop(s));
    }
  }

  /**
   * Date and durations: converts the specified string to a decimal value.
   * Returns an exception if the value is invalid.
   * @param s string to be converted
   * @param dur duration
   * @param ii input info
   * @return decimal
   * @throws QueryException query exception
   */
  BigDecimal toDecimal(final String s, final boolean dur, final InputInfo ii)
      throws QueryException {

    try {
      return new BigDecimal(s);
    } catch(final NumberFormatException ex) {
      throw (dur ? DURRANGE : DATERANGE).get(ii, type, chop(s));
    }
  }
}
