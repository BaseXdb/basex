package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Date item ({@code xs:date}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Dat extends ADate {
  /**
   * Constructor.
   * @param d date
   */
  public Dat(final ADate d) {
    super(AtomType.DAT, d);
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
  public Dat(final ADate d, final Dur a, final boolean p, final InputInfo ii)
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
    super(AtomType.DAT, d, XDATE, ii);
    date(d, XDATE, ii);
  }
}
