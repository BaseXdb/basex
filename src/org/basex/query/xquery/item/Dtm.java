package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.text.SimpleDateFormat;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

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
    super(Type.DTM);
    mon = d.mon;
    sec = d.sec;
    mil = d.mil;
    minus = d.minus;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param dt date
   * @throws XQException evaluation exception
   */
  public Dtm(final byte[] dt) throws XQException {
    super(Type.DTM);
    final int i = Token.indexOf(dt, 'T');
    if(i == -1) Err.date(type, XPDTM);
    date(Token.substring(dt, 0, i), XPDTM);
    time(Token.substring(dt, i + 1), XPDTM);
  }

  /**
   * Constructor.
   * @param tm time in milliseconds
   * @throws XQException evaluation exception
   */
  public Dtm(final Dec tm) throws XQException {
    this(Token.token(DATE.format(new java.util.Date(tm.itr()))));
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration
   * @param p plus/minus flag
   */
  public Dtm(final Date d, final Dur a, final boolean p) {
    this(d);
    calc(a, p);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    date(tb);
    tb.add('T');
    time(tb);
    zone(tb);
    return tb.finish();
  }
}
