package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.text.SimpleDateFormat;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * DateTime item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Dtm extends Date {
  /** Slider width. */
  public static final SimpleDateFormat DATE = new SimpleDateFormat(
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
   * @throws XQException evaluation exception
   */
  public Dtm(final byte[] dt) throws XQException {
    super(Type.DTM, dt, XDTM);
    final int i = Token.indexOf(dt, 'T');
    if(i == -1) Err.date(type, XDTM);
    date(Token.substring(dt, 0, i), XDTM);
    time(Token.substring(dt, i + 1), XDTM);
  }

  /**
   * Constructor.
   * @param tm time in milliseconds
   * @throws XQException evaluation exception
   */
  Dtm(final Dec tm) throws XQException {
    this(Token.token(DATE.format(new java.util.Date(tm.itr()))));
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration
   * @param p plus/minus flag
   * @throws XQException evaluation exception
   */
  public Dtm(final Date d, final Dur a, final boolean p) throws XQException {
    this(d);
    calc(a, p);
  }
}
