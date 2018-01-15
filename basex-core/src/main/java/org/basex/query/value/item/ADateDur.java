package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for dates and durations.
 *
 * @author BaseX Team 2005-18, BSD License
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

  /** Seconds and milliseconds. {@code null}: undefined. */
  public BigDecimal sec;

  /**
   * Constructor.
   * @param type item type
   */
  ADateDur(final Type type) {
    super(type);
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
  public abstract long hour();

  /**
   * Returns the minutes (0-59).
   * @return day
   */
  public abstract long minute();

  /**
   * Returns the seconds (0-59), including the fractional part.
   * @return day
   */
  public abstract BigDecimal sec();

  /**
   * Throws a date format exception.
   * @param input input
   * @param ex example format
   * @param info input info
   * @return date format exception
   */
  final QueryException dateError(final byte[] input, final String ex, final InputInfo info) {
    return DATEFORMAT_X_X_X.get(info, type, input, ex);
  }

  /**
   * Date and durations: converts the specified string to an integer value.
   * Returns an exception if the value is invalid.
   * @param string string to be converted
   * @param dur duration
   * @param info input info
   * @return long value
   * @throws QueryException query exception
   */
  final long toLong(final String string, final boolean dur, final InputInfo info)
      throws QueryException {
    try {
      return Long.parseLong(string);
    } catch(final NumberFormatException ex) {
      Util.debug(ex);
      throw (dur ? DURRANGE_X_X : DATERANGE_X_X).get(info, type, chop(string, info));
    }
  }

  /**
   * Date and durations: converts the specified string to a decimal value.
   * Returns an exception if the value is invalid.
   * @param string string to be converted
   * @param dur duration
   * @param info input info
   * @return decimal
   * @throws QueryException query exception
   */
  final BigDecimal toDecimal(final String string, final boolean dur, final InputInfo info)
      throws QueryException {

    try {
      return new BigDecimal(string);
    } catch(final NumberFormatException ex) {
      Util.debug(ex);
      throw (dur ? DURRANGE_X_X : DATERANGE_X_X).get(info, type, chop(string, info));
    }
  }
}
