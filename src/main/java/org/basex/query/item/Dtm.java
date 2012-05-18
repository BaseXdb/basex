package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.Util.*;

import java.text.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.util.*;

/**
 * DateTime item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Dtm extends Date {
  /** Unified date format. */
  public static final SimpleDateFormat FORMAT;

  static {
    FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * Constructor.
   * @param d date
   */
  public Dtm(final Date d) {
    super(AtomType.DTM, d);
    if(xc.getHour() == UNDEF) {
      xc.setHour(0);
      xc.setMinute(0);
      xc.setSecond(0);
    }
  }

  /**
   * Constructor.
   * @param dt date
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final byte[] dt, final InputInfo ii) throws QueryException {
    super(AtomType.DTM, dt, XDTM, ii);
    final int i = Token.indexOf(dt, 'T');
    if(i == -1) dateErr(dt, XDTM, ii);
    date(Token.substring(dt, 0, i), XDTM, ii);
    time(Token.substring(dt, i + 1), XDTM, ii);
  }

  /**
   * Constructor.
   * @param tm time in milliseconds
   * @param ii input info
   * @throws QueryException query exception
   */
  Dtm(final Int tm, final InputInfo ii) throws QueryException {
    this(token(formatDate(new java.util.Date(tm.itr()), FORMAT)), ii);
  }

  /**
   * Constructor.
   * @param tm milliseconds since January 1, 1970, 00:00:00 GMT
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final long tm, final InputInfo ii) throws QueryException {
    this(token(formatDate(new java.util.Date(tm), FORMAT)), ii);
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration
   * @param p plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final Date d, final Dur a, final boolean p, final InputInfo ii)
      throws QueryException {
    this(d);
    calc(a, p, ii);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Dtm)) return false;
    final Dtm dtm = (Dtm) cmp;
    return type == dtm.type && xc.equals(dtm.xc);
  }

  /**
   * Parses the specified date and returns its time in milliseconds.
   * Returns {@code null} if it cannot be converted.
   * @param date date to be parsed
   * @return time in milliseconds
   */
  public static long parse(final String date) {
    try {
      return parseDate(date, FORMAT).getTime();
    } catch(final ParseException ex) {
      Util.errln(ex);
      return 0;
    }
  }
}
