package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for dates and durations.
 *
 * @author BaseX Team, BSD License
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
   * @return month
   */
  public abstract long mon();

  /**
   * Returns the days.
   * @return day
   */
  public abstract long day();

  /**
   * Returns the hours (0-23).
   * @return hour
   */
  public abstract long hour();

  /**
   * Returns the minutes (0-59).
   * @return minute
   */
  public abstract long minute();

  /**
   * Returns the seconds (0-59), including the fractional part.
   * @return seconds
   */
  public abstract BigDecimal seconds();

  /**
   * Returns a date format exception.
   * @param input input
   * @param ex example format
   * @param info input info (can be {@code null})
   * @return date format exception
   */
  final QueryException dateError(final byte[] input, final String ex, final InputInfo info) {
    return DATEFORMAT_X_X_X.get(info, type, ex, input);
  }

  /**
   * Date and durations: converts a scanned substring to an integer value.
   * @param ds date scanner
   * @param start start position
   * @param end end position (exclusive)
   * @param info input info (can be {@code null})
   * @return long value
   * @throws QueryException query exception
   */
  final long toLong(final DateScanner ds, final int start, final int end, final InputInfo info)
      throws QueryException {
    final long v = ds.integer(start, end);
    if(v == Long.MIN_VALUE) {
      throw (this instanceof Dur ? DURRANGE_X_X : DATERANGE_X_X).get(info, type,
          ds.string(start, end));
    }
    return v;
  }
}
