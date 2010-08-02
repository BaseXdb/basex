package org.basex.query.item;

import static org.basex.query.QueryText.*;
import java.text.SimpleDateFormat;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * DateTime item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final byte[] dt, final InputInfo ii) throws QueryException {
    super(Type.DTM, dt, XDTM, ii);
    final int i = Token.indexOf(dt, 'T');
    if(i == -1) dateErr(dt, type, XDTM, ii);
    date(Token.substring(dt, 0, i), XDTM, ii);
    time(Token.substring(dt, i + 1), XDTM, ii);
  }

  /**
   * Constructor.
   * @param tm time in milliseconds
   * @param ii input info
   * @throws QueryException query exception
   */
  Dtm(final Itr tm, final InputInfo ii) throws QueryException {
    this(Token.token(DATE.format(new java.util.Date(tm.itr(ii)))), ii);
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
}
