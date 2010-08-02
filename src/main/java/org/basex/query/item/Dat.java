package org.basex.query.item;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * Date item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dat(final Date d, final Dur a, final boolean p, final InputInfo ii)
      throws QueryException {
    this(d);
    calc(a, p, ii);
  }

  /**
   * Constructor.
   * @param d date
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dat(final byte[] d, final InputInfo ii) throws QueryException {
    super(Type.DAT, d, XDATE, ii);
    date(d, XDATE, ii);
  }
}
