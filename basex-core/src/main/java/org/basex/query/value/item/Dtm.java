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
    if(type == BasicType.DATE_TIME_STAMP && !hasTz()) throw MISSINGZONE_X.get(info, date);
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
    super(BasicType.DATE_TIME, date);

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

  /**
   * Constructor.
   * @param type item type
   */
  private Dtm(final Type type) {
    super(type);
  }

  @Override
  public Dtm timeZone(final DTDur dur, final boolean undefined, final InputInfo info)
      throws QueryException {
    final Dtm dtm = new Dtm(this, BasicType.DATE_TIME, info);
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
      return new Dtm(Token.token(DateTime.format(new Date(ms))), BasicType.DATE_TIME_STAMP, null);
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public boolean comparable(final Item item) {
    return item instanceof Dtm;
  }

  /**
   * Constructs a Gregorian value from the supplied component values.
   *
   * <p>This method assumes that all component combinations and value ranges
   * have already been validated by the caller. In particular, it does not
   * verify:</p>
   * <ul>
   *   <li>that the combination of components matches the target type,</li>
   *   <li>that the component values are within their lexical or calendar ranges,</li>
   *   <li>that a timezone is present when required (e.g., for {@code xs:dateTimeStamp}).</li>
   * </ul>
   *
   * <p>Components must be supplied in their lexical representation:
   * month 1–12, day 1–31, hours 0–23, minutes 0–59, seconds in [0–60).
   * For BC years, pass a negative year or 0 (for year 0). Undefined components must
   * be {@code null}.</p>
   *
   * <p>This method is intended as a low-level construction helper for
   * {@link org.basex.query.func.fn.FnBuildDateTime}, which performs all
   * required validation and determines the appropriate target type.</p>
   *
   * @param targetType target type
   * @param year year (can be {@code null})
   * @param month month (1–12, can be {@code null})
   * @param day day (1–31, can be {@code null})
   * @param hours hours (0–23, can be {@code null})
   * @param minutes minutes (0–59, can be {@code null})
   * @param seconds seconds ([0–60), can be {@code null})
   * @param zone timezone (can be {@code null})
   * @param info input info (can be {@code null})
   * @return constructed date/time item
   * @throws QueryException query exception
   */
  public static ADate buildUnchecked(final BasicType targetType,
      final Long year, final Long month, final Long day,
      final Long hours, final Long minutes, final BigDecimal seconds,
      final DTDur zone, final InputInfo info) throws QueryException {
    final Dtm base = new Dtm(targetType);
    base.year    = year    == null ? Long.MAX_VALUE : year;
    base.month   = month   == null ? -1             : (byte) (month - 1);
    base.day     = day     == null ? -1             : (byte) (day - 1);
    base.hour    = hours   == null ? -1             : hours.byteValue();
    base.minute  = minutes == null ? -1             : minutes.byteValue();
    base.seconds = seconds;
    base.tz(zone, zone == null, info);
    return switch(targetType) {
      case DATE_TIME, DATE_TIME_STAMP -> base;
      case DATE                       -> new Dat(base);
      case TIME                       -> new Tim(base);
      default                         -> new GDt(base, targetType);
    };
  }
}
