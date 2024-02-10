package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.math.*;
import java.util.regex.*;

import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * YearMonth duration ({@code xs:yearMonthDuration}).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class YMDur extends Dur {
  /**
   * Constructor.
   * @param value duration item
   */
  public YMDur(final Dur value) {
    super(AtomType.YEAR_MONTH_DURATION);
    months = value.months;
    seconds = BigDecimal.ZERO;
  }

  /**
   * Constructor.
   * @param value duration item
   * @param dur duration to be added/subtracted
   * @param plus plus/minus flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public YMDur(final YMDur value, final YMDur dur, final boolean plus, final InputInfo info)
      throws QueryException {

    this(value);
    final double d = (double) months + (plus ? dur.months : -dur.months);
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw MONTHRANGE_X.get(info, d);
    months += plus ? dur.months : -dur.months;
  }

  /**
   * Constructor.
   * @param value duration item
   * @param factor factor
   * @param mult multiplication/division flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public YMDur(final Dur value, final double factor, final boolean mult, final InputInfo info)
      throws QueryException {

    this(value);
    if(Double.isNaN(factor)) throw DATECALC_X_X.get(info, description(), factor);
    if(mult ? Double.isInfinite(factor) : factor == 0) throw DATEZERO_X_X.get(info, type, factor);
    final double d = mult ? months * factor : months / factor;
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw MONTHRANGE_X.get(info, d);
    months = StrictMath.round(d);
  }

  /**
   * Constructor.
   * @param value value
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public YMDur(final byte[] value, final InputInfo info) throws QueryException {
    super(AtomType.YEAR_MONTH_DURATION);
    final String val = Token.string(value).trim();
    final Matcher mt = YMD.matcher(val);
    if(!mt.matches() || Strings.endsWith(val, 'P')) throw dateError(value, XYMD, info);
    yearMonth(value, mt, info);
    seconds = BigDecimal.ZERO;
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeToken(string(null));
  }

  /**
   * Returns the years and months.
   * @return year
   */
  public long ymd() {
    return months;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    if(months < 0) tb.add('-');
    date(tb);
    if(months == 0) tb.add("0M");
    return tb.finish();
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return item.type == type ? Long.signum(months - ((Dur) item).months) :
      super.compare(item, coll, transitive, ii);
  }
}
