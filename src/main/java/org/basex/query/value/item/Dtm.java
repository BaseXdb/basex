package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * DateTime item ({@code xs:dateTime}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Dtm extends ADate {
  /**
   * Constructor.
   * @param d date
   */
  public Dtm(final ADate d) {
    super(AtomType.DTM, d);
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
    super(AtomType.DTM, dt, XDTM, ii);
    final int i = Token.indexOf(dt, 'T');
    if(i == -1) dateErr(dt, XDTM, ii);
    date(Token.substring(dt, 0, i), XDTM, ii);
    time(Token.substring(dt, i + 1), XDTM, ii);
  }

  /**
   * Constructor.
   * @param tm milliseconds since January 1, 1970, 00:00:00 GMT
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final long tm, final InputInfo ii) throws QueryException {
    this(token(DateTime.format(new java.util.Date(tm), DateTime.FULL)), ii);
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration
   * @param p plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final ADate d, final Dur a, final boolean p, final InputInfo ii)
      throws QueryException {
    this(d);
    calc(a, p, ii);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Dtm)) return false;
    final Dtm dtm = (Dtm) cmp;
    return type == dtm.type && xc.equals(dtm.xc);
  }
}
