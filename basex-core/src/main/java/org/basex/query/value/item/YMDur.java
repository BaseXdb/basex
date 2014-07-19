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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class YMDur extends Dur {
  /** YearMonth pattern. */
  private static final Pattern DUR = Pattern.compile("(-?)P(" + DP + "Y)?(" + DP + "M)?");

  /**
   * Constructor.
   * @param value duration item
   */
  public YMDur(final Dur value) {
    super(AtomType.YMD);
    mon = value.mon;
    sec = BigDecimal.ZERO;
  }

  /**
   * Constructor.
   * @param value duration item
   * @param dur duration to be added/subtracted
   * @param plus plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public YMDur(final YMDur value, final YMDur dur, final boolean plus, final InputInfo ii)
      throws QueryException {

    this(value);
    final double d = (double) mon + (plus ? dur.mon : -dur.mon);
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw MONTHRANGE.get(ii, d);
    mon += plus ? dur.mon : -dur.mon;
  }

  /**
   * Constructor.
   * @param value duration item
   * @param factor factor
   * @param mult multiplication/division flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public YMDur(final Dur value, final double factor, final boolean mult, final InputInfo ii)
      throws QueryException {

    this(value);
    if(Double.isNaN(factor)) throw DATECALC.get(ii, description(), factor);
    if(mult ? Double.isInfinite(factor) : factor == 0) throw DATEZERO.get(ii, type, factor);
    final double d = mult ? mon * factor : mon / factor;
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw MONTHRANGE.get(ii, d);
    mon = StrictMath.round(d);
  }

  /**
   * Constructor.
   * @param value value
   * @param ii input info
   * @throws QueryException query exception
   */
  public YMDur(final byte[] value, final InputInfo ii) throws QueryException {
    super(AtomType.YMD);
    final String val = Token.string(value).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P")) throw dateError(value, XYMD, ii);
    yearMonth(value, mt, ii);
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
