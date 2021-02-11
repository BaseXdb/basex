package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.value.type.AtomType.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple date item, used for {@code xs:gYearMonth}, {@code xs:gYear},
 * {@code xs:gMonthDay}, {@code xs:gDay} and {@code xs:gMonth}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class GDt extends ADate {
  /** Date pattern. */
  private static final Type[] TYPES = { G_YEAR, G_YEAR_MONTH, G_MONTH, G_MONTH_DAY, G_DAY };
  /** Date patterns. */
  private static final Pattern[] PATTERNS = {
    Pattern.compile(YEAR + ZONE),
    Pattern.compile(YEAR + '-' + DD + ZONE),
    Pattern.compile("--" + DD + ZONE),
    Pattern.compile("--" + DD + '-' + DD + ZONE),
    Pattern.compile("---" + DD + ZONE)
  };
  /** Date pattern. */
  private static final String[] EXAMPLES = { XYEA, XYMO, XMON, XMDA, XDAY };
  /** Date zones. */
  private static final int[] ZONES = { 3, 4, 2, 3, 2 };

  /**
   * Constructor.
   * @param date date
   * @param type item type
   */
  public GDt(final ADate date, final Type type) {
    super(type, date);
    if(type != G_YEAR && type != G_YEAR_MONTH) yea = Long.MAX_VALUE;
    if(type != G_MONTH && type != G_YEAR_MONTH && type != G_MONTH_DAY) mon = -1;
    if(type != G_DAY && type != G_MONTH_DAY) day = -1;
    hou = -1;
    min = -1;
    sec = null;
  }

  /**
   * Constructor.
   * @param date date
   * @param type item type
   * @param ii input info
   * @throws QueryException query exception
   */
  public GDt(final byte[] date, final Type type, final InputInfo ii) throws QueryException {
    super(type);

    final String dt = Token.string(date).trim();
    final int i = type(type);
    final Matcher mt = PATTERNS[i].matcher(dt);
    if(!mt.matches()) throw dateError(date, EXAMPLES[i], ii);

    if(i < 2) {
      yea = toLong(mt.group(1), false, ii);
      // +1 is added to BC values to simplify computations
      if(yea < 0) yea++;
      if(yea < MIN_YEAR || yea >= MAX_YEAR)
        throw DATERANGE_X_X.get(ii, type, normalize(date, ii));
    }
    if(i > 0 && i < 4) {
      mon = (byte) (Strings.toLong(mt.group(i == 1 ? 3 : 1)) - 1);
      if(mon < 0 || mon > 11) throw dateError(date, EXAMPLES[i], ii);
    }
    if(i > 2) {
      day = (byte) (Strings.toLong(mt.group(i == 3 ? 2 : 1)) - 1);
      final int m = Math.max(mon, 0);
      if(day < 0 || day >= DAYS[m] + (m == 1 ? 1 : 0)) throw dateError(date, EXAMPLES[i], ii);
    }
    zone(mt, ZONES[i], date, ii);
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
  public void timeZone(final DTDur zone, final boolean d, final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    throw diffError(item, this, ii);
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    if(yea == Long.MAX_VALUE) {
      tb.add('-');
    } else {
      if(yea <= 0) tb.add('-');
      prefix(tb, Math.abs(yea()), 4);
    }
    if(mon >= 0 || day >= 0) tb.add('-');
    if(mon >= 0) prefix(tb, mon + 1, 2);
    if(day >= 0) tb.add('-');
    if(day >= 0) prefix(tb, day + 1, 2);

    zone(tb);
    return tb.finish();
  }
}
