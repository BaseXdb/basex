package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple date item, used for {@code xs:gYearMonth}, {@code xs:gYear},
 * {@code xs:gMonthDay}, {@code xs:gDay} and {@code xs:gMonth}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class GDt extends ADate {
  /** Date pattern. */
  private static final Type[] TYPES = {
    AtomType.YEA, AtomType.YMO, AtomType.MON, AtomType.MDA, AtomType.DAY,
  };
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
   * @param d date
   * @param t data type
   */
  public GDt(final ADate d, final Type t) {
    super(t, d);
    if(t != AtomType.YEA && t != AtomType.YMO) yea = Long.MAX_VALUE;
    if(t != AtomType.MON && t != AtomType.YMO && t != AtomType.MDA) mon = -1;
    if(t != AtomType.DAY && t != AtomType.MDA) day = -1;
    hou = -1;
    min = -1;
    sec = null;
  }

  /**
   * Constructor.
   * @param d date
   * @param t data type
   * @param ii input info
   * @throws QueryException query exception
   */
  public GDt(final byte[] d, final Type t, final InputInfo ii) throws QueryException {
    super(t);

    final String dt = Token.string(d).trim();
    final int i = type(t);
    final Matcher mt = PATTERNS[i].matcher(dt);
    if(!mt.matches()) dateErr(d, EXAMPLES[i], ii);

    if(i < 2) {
      yea = toLong(mt.group(1), false, ii);
      // +1 is added to BC values to simplify computations
      if(yea < 0) yea++;
      if(yea < MIN_YEAR || yea >= MAX_YEAR) DATERANGE.thrw(ii, type, d);
    }
    if(i > 0 && i < 4) {
      mon = (byte) (Token.toLong(mt.group(i == 1 ? 3 : 1)) - 1);
      if(mon < 0 || mon > 11) dateErr(d, EXAMPLES[i], ii);
    }
    if(i > 2) {
      day = (byte) (Token.toLong(mt.group(i == 3 ? 2 : 1)) - 1);
      final int m = Math.max(mon, 0);
      if(day < 0 || day >= DAYS[m] + (m == 1 ? 1 : 0)) dateErr(d, EXAMPLES[i], ii);
    }
    zone(mt, ZONES[i], d, ii);
  }

  /**
   * Returns the offset for the specified type.
   * @param type type
   * @return offset
   */
  private static int type(final Type type) {
    for(int t = 0; t < TYPES.length; ++t) if(TYPES[t] == type) return t;
    throw Util.notexpected();
  }

  @Override
  public void timeZone(final DTDur tz, final boolean d, final InputInfo ii)
      throws QueryException {
    Util.notexpected();
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    throw Err.diff(ii, it, this);
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    if(yea != Long.MAX_VALUE) {
      if(yea <= 0) tb.add('-');
      prefix(tb, Math.abs(yea()), 4);
    } else {
      tb.add('-');
    }
    if(mon >= 0 || day >= 0) tb.add('-');
    if(mon >= 0) prefix(tb, mon + 1, 2);
    if(day >= 0) tb.add('-');
    if(day >= 0) prefix(tb, day + 1, 2);

    zone(tb);
    return tb.finish();
  }
}
