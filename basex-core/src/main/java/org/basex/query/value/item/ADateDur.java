package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for dates and durations.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class ADateDur extends Item {
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
   * @param info input info (can be {@code null})
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
   * @param info input info (can be {@code null})
   * @return long value
   * @throws QueryException query exception
   */
  final long toLong(final String string, final boolean dur, final InputInfo info)
      throws QueryException {
    try {
      return Long.parseLong(string);
    } catch(final NumberFormatException ex) {
      Util.debug(ex);
      throw (dur ? DURRANGE_X_X : DATERANGE_X_X).get(info, type, string);
    }
  }

  /**
   * Date and durations: converts the specified string to a decimal value.
   * Returns an exception if the value is invalid.
   * @param string string to be converted
   * @param dur duration
   * @param info input info (can be {@code null})
   * @return decimal
   * @throws QueryException query exception
   */
  final BigDecimal toDecimal(final String string, final boolean dur, final InputInfo info)
      throws QueryException {

    try {
      return new BigDecimal(string);
    } catch(final NumberFormatException ex) {
      Util.debug(ex);
      throw (dur ? DURRANGE_X_X : DATERANGE_X_X).get(info, type, string);
    }
  }
}
