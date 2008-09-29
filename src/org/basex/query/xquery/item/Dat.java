package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;

/**
 * Date item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Dat extends Date {
  /**
   * Constructor.
   * @param d date
   */
  public Dat(final Date d) {
    super(Type.DAT, d);
    xc.setTime(UNDEF, UNDEF, UNDEF);
    xc.setMillisecond(UNDEF);
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration
   * @param p plus/minus flag
   * @throws XQException evaluation exception
   */
  public Dat(final Date d, final Dur a, final boolean p) throws XQException {
    this(d);
    calc(a, p);
  }

  /**
   * Constructor.
   * @param d date
   * @throws XQException evaluation exception
   */
  public Dat(final byte[] d) throws XQException {
    super(Type.DAT, d, XPDATE);
    date(d, XPDATE);
  }
}
