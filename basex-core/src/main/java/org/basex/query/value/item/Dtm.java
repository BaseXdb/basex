package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * DateTime item ({@code xs:dateTime} and {@code xs:dateTimeStamp}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Dtm extends ADate {
  /** UNIX time. */
  public static final Dtm ZERO = get(0);

  /**
   * Constructor.
   * @param date date
   * @param type item type
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dtm(final ADate date, final Type type, final InputInfo info) throws QueryException {
    super(type, date);
    if(type == AtomType.DATE_TIME_STAMP && !hasTz()) throw MISSINGZONE_X.get(info, date);
    if(hour == -1) {
      hour = 0;
      minute = 0;
      seconds = BigDecimal.ZERO;
    }
  }

  /**
   * Constructor.
   * @param date date
   * @param time time
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dtm(final Dat date, final Tim time, final InputInfo info) throws QueryException {
    super(AtomType.DATE_TIME, date);

    hour = time.hour;
    minute = time.minute;
    seconds = time.seconds;
    if(tz == Short.MAX_VALUE) {
      tz = time.tz;
    } else if(tz != time.tz && time.tz != Short.MAX_VALUE) {
      throw FUNZONE_X_X.get(info, date, time);
    }
  }

  /**
   * Constructor.
   * @param dateTime date time
   * @param type item type
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dtm(final byte[] dateTime, final Type type, final InputInfo info) throws QueryException {
    super(type);
    final int i = Token.indexOf(dateTime, 'T');
    if(i == -1) throw dateError(dateTime, XDTM, info);
    date(Token.substring(dateTime, 0, i), XDTM, info);
    time(Token.substring(dateTime, i + 1), XDTM, info);
  }

  /**
   * Constructor.
   * @param dateTime date
   * @param dur duration
   * @param plus plus/minus flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dtm(final Dtm dateTime, final Dur dur, final boolean plus, final InputInfo info)
      throws QueryException {

    this(dateTime, dateTime.type, info);
    if(dur instanceof final DTDur dtd) {
      calc(dtd, plus);
      if(year <= MIN_YEAR || year > MAX_YEAR) throw YEARRANGE_X.get(info, year);
    } else {
      calc((YMDur) dur, plus, info);
    }
  }

  @Override
  public Dtm timeZone(final DTDur dur, final boolean undefined, final InputInfo info)
      throws QueryException {
    final Dtm dtm = new Dtm(this, AtomType.DATE_TIME, info);
    dtm.tz(dur, undefined, info);
    return dtm;
  }

  /**
   * Returns a dateTime item for the specified milliseconds.
   * @param ms milliseconds since January 1, 1970, 00:00:00 GMT
   * @return dateTime instance
   */
  public static Dtm get(final long ms) {
    try {
      return new Dtm(Token.token(DateTime.format(new Date(ms))), AtomType.DATE_TIME_STAMP, null);
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }
}
