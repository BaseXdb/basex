package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.math.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * YearMonth duration ({@code xs:yearMonthDuration}).
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class YMDur extends Dur {
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
   * @param info input info
   * @throws QueryException query exception
   */
  public YMDur(final YMDur value, final YMDur dur, final boolean plus, final InputInfo info)
      throws QueryException {

    this(value);
    final double d = (double) mon + (plus ? dur.mon : -dur.mon);
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw MONTHRANGE_X.get(info, d);
    mon += plus ? dur.mon : -dur.mon;
  }

  /**
   * Constructor.
   * @param value duration item
   * @param factor factor
   * @param mult multiplication/division flag
   * @param info input info
   * @throws QueryException query exception
   */
  public YMDur(final Dur value, final double factor, final boolean mult, final InputInfo info)
      throws QueryException {

    this(value);
    if(Double.isNaN(factor)) throw DATECALC_X_X.get(info, description(), factor);
    if(mult ? Double.isInfinite(factor) : factor == 0) throw DATEZERO_X_X.get(info, type, factor);
    final double d = mult ? mon * factor : mon / factor;
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw MONTHRANGE_X.get(info, d);
    mon = StrictMath.round(d);
  }

  /**
   * Constructor.
   * @param value value
   * @param info input info
   * @throws QueryException query exception
   */
  public YMDur(final byte[] value, final InputInfo info) throws QueryException {
    super(AtomType.YMD);
    final String val = Token.string(value).trim();
    final Matcher mt = YMD.matcher(val);
    if(!mt.matches() || val.endsWith("P")) throw dateError(value, XYMD, info);
    yearMonth(value, mt, info);
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
  public byte[] string(final InputInfo info) {
    final TokenBuilder tb = new TokenBuilder();
    if(mon < 0) tb.add('-');
    date(tb);
    if(mon == 0) tb.add("0M");
    return tb.finish();
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    if(item.type != type) throw diffError(item, this, info);
    final long m = mon - ((Dur) item).mon;
    return m < 0 ? -1 : m > 0 ? 1 : 0;
  }
}
