package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.math.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * YearMonth duration ({@code xs:yearMonthDuration}).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class YMDur extends Dur {
  /** YearMonth pattern. */
  private static final Pattern DUR =
      Pattern.compile("(-?)P(" + DP + "Y)?(" + DP + "M)?");

  /**
   * Constructor.
   * @param it duration item
   */
  public YMDur(final Dur it) {
    super(AtomType.YMD);
    mon = it.mon;
    sec = BigDecimal.ZERO;
  }

  /**
   * Constructor.
   * @param it duration item
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public YMDur(final YMDur it, final YMDur a, final boolean p,
      final InputInfo ii) throws QueryException {

    this(it);
    final double d = (double) mon + (p ? a.mon : -a.mon);
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw DURADDRANGE.get(ii, type);
    mon += p ? a.mon : -a.mon;
  }

  /**
   * Constructor.
   * @param it duration item
   * @param f factor
   * @param m multiplication/division flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public YMDur(final Dur it, final double f, final boolean m, final InputInfo ii)
      throws QueryException {

    this(it);
    if(Double.isNaN(f)) throw DATECALC.get(ii, description(), f);
    if(m ? Double.isInfinite(f) : f == 0) throw DATEZERO.get(ii, description());
    final double d = m ? mon * f : mon / f;
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw DURADDRANGE.get(ii, type);
    mon = StrictMath.round(d);
  }

  /**
   * Constructor.
   * @param vl value
   * @param ii input info
   * @throws QueryException query exception
   */
  public YMDur(final byte[] vl, final InputInfo ii) throws QueryException {
    super(AtomType.YMD);
    final String val = Token.string(vl).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P")) dateErr(vl, XYMD, ii);
    yearMonth(vl, mt, ii);
    sec = BigDecimal.ZERO;
  }

  /**
   * Returns the years and months.
   * @return year
   */
  public long ymd() {
    return mon;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    if(mon < 0) tb.add('-');
    date(tb);
    if(mon == 0) tb.add("0M");
    return tb.finish();
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    if(it.type != type) throw diffError(ii, it, this);
    final long m = mon - ((Dur) it).mon;
    return m < 0 ? -1 : m > 0 ? 1 : 0;
  }
}
