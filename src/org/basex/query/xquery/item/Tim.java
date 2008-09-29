package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;

/**
 * Time item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Tim extends Date {
  /**
   * Constructor.
   * @param d date
   */
  public Tim(final Date d) {
    super(Type.TIM, d);
    xc.setYear(UNDEF);
    xc.setMonth(UNDEF);
    xc.setDay(UNDEF);
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   * @throws XQException evaluation exception
   */
  public Tim(final Tim d, final DTd a, final boolean p) throws XQException {
    this(d);
    calc(a, p);
  }

  /**
   * Constructor.
   * @param tim time
   * @throws XQException evaluation exception
   */
  public Tim(final byte[] tim) throws XQException {
    super(Type.TIM, tim, XPTIME);
    time(tim, XPTIME);
  }
}
