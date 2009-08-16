package org.basex.query.item;

import static org.basex.query.QueryText.*;
import java.text.SimpleDateFormat;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * DateTime item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Dtm extends Date {
  /** Date format. */
  private static final SimpleDateFormat DATE = new SimpleDateFormat(
      "yyyy-MM-dd'T'hh:mm:ss.S");

  /**
   * Constructor.
   * @param d date
   */
  public Dtm(final Date d) {
    super(Type.DTM, d);
    if(xc.getHour() == UNDEF) {
      xc.setHour(0);
      xc.setMinute(0);
      xc.setSecond(0);
    }
  }

  /**
   * Constructor.
   * @param dt date
   * @throws QueryException query exception
   */
  public Dtm(final byte[] dt) throws QueryException {
    super(Type.DTM, dt, XDTM);
    final int i = Token.indexOf(dt, 'T');
    if(i == -1) Err.date(dt, type, XDTM);
    date(Token.substring(dt, 0, i), XDTM);
    time(Token.substring(dt, i + 1), XDTM);
  }

  /**
   * Constructor.
   * @param tm time in milliseconds
   * @throws QueryException query exception
   */
  Dtm(final Dec tm) throws QueryException {
    this(Token.token(DATE.format(new java.util.Date(tm.itr()))));
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration
   * @param p plus/minus flag
   * @throws QueryException query exception
   */
  public Dtm(final Date d, final Dur a, final boolean p) throws QueryException {
    this(d);
    calc(a, p);
  }
}
