package org.basex.query.item;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryException;

/**
 * Time item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Tim extends Date {
  /**
   * Constructor.
   * @param d date
   */
  Tim(final Date d) {
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
   * @throws QueryException evaluation exception
   */
  public Tim(final Tim d, final DTd a, final boolean p) throws QueryException {
    this(d);
    calc(a, p);
  }

  /**
   * Constructor.
   * @param tim time
   * @throws QueryException evaluation exception
   */
  public Tim(final byte[] tim) throws QueryException {
    super(Type.TIM, tim, XTIME);
    time(tim, XTIME);
  }
}
