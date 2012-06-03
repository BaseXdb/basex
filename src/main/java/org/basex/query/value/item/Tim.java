package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Time item ({@code xs:time}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Tim extends ADate {
  /**
   * Constructor.
   * @param d date
   */
  public Tim(final ADate d) {
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
