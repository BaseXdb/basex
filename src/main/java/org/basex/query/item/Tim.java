package org.basex.query.item;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * Time item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Tim extends Date {
  /**
   * Constructor.
   * @param d date
   */
  public Tim(final Date d) {
    super(AtomType.TIM, d);
    xc.setYear(UNDEF);
    xc.setMonth(UNDEF);
    xc.setDay(UNDEF);
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public Tim(final Tim d, final DTd a, final boolean p, final InputInfo ii)
      throws QueryException {
    this(d);
    calc(a, p, ii);
  }

  /**
   * Constructor.
   * @param tim time
   * @param ii input info
   * @throws QueryException query exception
   */
  public Tim(final byte[] tim, final InputInfo ii) throws QueryException {
    super(AtomType.TIM, tim, XTIME, ii);
    time(tim, XTIME, ii);
  }
}
