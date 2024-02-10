package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * DateTime item ({@code xs:dateTime}).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Dtm extends ADate {
  /**
   * Constructor.
   * @param date date
   */
  public Dtm(final ADate date) {
    super(AtomType.DATE_TIME, date);
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
   * @param date date
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dtm(final byte[] date, final InputInfo info) throws QueryException {
    super(AtomType.DATE_TIME);
    final int i = Token.indexOf(date, 'T');
    if(i == -1) throw dateError(date, XDTM, info);
    date(Token.substring(date, 0, i), XDTM, info);
    time(Token.substring(date, i + 1), XDTM, info);
  }

  /**
   * Constructor.
   * @param date date
   * @param dur duration
   * @param plus plus/minus flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dtm(final Dtm date, final Dur dur, final boolean plus, final InputInfo info)
      throws QueryException {

    this(date);
    if(dur instanceof DTDur) {
      calc((DTDur) dur, plus);
      if(year <= MIN_YEAR || year > MAX_YEAR) throw YEARRANGE_X.get(info, year);
    } else {
      calc((YMDur) dur, plus, info);
    }
  }

  @Override
  public Dtm timeZone(final DTDur dur, final boolean undefined, final InputInfo info)
      throws QueryException {
    final Dtm dtm = new Dtm(this);
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
      return new Dtm(Token.token(DateTime.format(new Date(ms))), null);
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }
}
