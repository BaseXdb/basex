package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.value.type.BasicType.*;

import java.time.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple date item, used for {@code xs:gYearMonth}, {@code xs:gYear},
 * {@code xs:gMonthDay}, {@code xs:gDay} and {@code xs:gMonth}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GDt extends ADate {
  /** Date types. */
  private static final Type[] TYPES = { G_YEAR, G_YEAR_MONTH, G_MONTH, G_MONTH_DAY, G_DAY };
  /** Date examples. */
  private static final String[] EXAMPLES = { XYEA, XYMO, XMON, XMDA, XDAY };

  /**
   * Constructor.
   * @param date date
   * @param type item type
   */
  public GDt(final ADate date, final Type type) {
    super(type, date);
    if(type != G_YEAR && type != G_YEAR_MONTH) { defined &= ~YEA; year = Long.MAX_VALUE; }
    if(type != G_MONTH && type != G_YEAR_MONTH && type != G_MONTH_DAY) {
      defined &= ~MON;
      month = -1;
    }
    if(type != G_DAY && type != G_MONTH_DAY) { defined &= ~DAY; day = -1; }
    defined &= ~(HRS | MIN | SEC);
    hour = -1;
    minute = -1;
    seconds = null;
  }

  /**
   * Constructor.
   * @param date date
   * @param type item type
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public GDt(final byte[] date, final Type type, final InputInfo info) throws QueryException {
    super(type);

    final DateScanner ds = new DateScanner(date);
    final int i = type(type);
    if(!scan(ds, i) || !ds.zone() || !ds.done()) throw dateError(date, EXAMPLES[i], info);

    if(i < 2) {
      year = toLong(ds, ds.yearStart, ds.yearEnd, info);
      defined |= YEA;
      if(year < Year.MIN_VALUE || year > Year.MAX_VALUE) throw DATERANGE_X_X.get(info, type, date);
    }
    if(i > 0 && i < 4) {
      defined |= MON;
      if(month < 1 || month > 12) throw dateError(date, EXAMPLES[i], info);
    }
    if(i > 2) {
      defined |= DAY;
      if(day < 1 || day > maxDaysOfMonth(has(MON) ? month : 1))
        throw dateError(date, EXAMPLES[i], info);
    }
    zone(ds, date, info);
  }

  /**
   * Scans the components of the specified type.
   * @param ds date scanner
   * @param i type offset
   * @return success flag
   */
  private boolean scan(final DateScanner ds, final int i) {
    return switch(i) {
      case 0  -> ds.year();
      case 1  -> scan(ds, 0) && ds.consume('-') && scanMonth(ds);
      case 2  -> ds.consume('-') && ds.consume('-') && scanMonth(ds);
      case 3  -> scan(ds, 2) && ds.consume('-') && scanDay(ds);
      default -> ds.consume('-') && ds.consume('-') && ds.consume('-') && scanDay(ds);
    };
  }

  /**
   * Returns the offset for the specified type.
   * @param type type
   * @return offset
   */
  private static int type(final Type type) {
    final int tl = TYPES.length;
    for(int t = 0; t < tl; t++) {
      if(TYPES[t] == type) return t;
    }
    throw Util.notExpected();
  }

  @Override
  public GDt timeZone(final DTDur dur, final boolean undefined, final InputInfo info) {
    throw Util.notExpected();
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    if(!has(YEA)) {
      tb.add('-');
    } else {
      if(year < 0) tb.add('-');
      prefix(tb, Math.abs(yea()), 4);
    }
    if(has(MON) || has(DAY)) tb.add('-');
    if(has(MON)) prefix(tb, month, 2);
    if(has(DAY)) prefix(tb.add('-'), day, 2);

    zone(tb);
    return tb.finish();
  }
}
