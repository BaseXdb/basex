package org.basex.query.item;

import static org.basex.query.QueryText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Simple date item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DSim extends Date {
  /** Date pattern. */
  private static final Type[] TYPES = {
    Type.YEA, Type.YMO, Type.MON, Type.MDA, Type.DAY,
  };
  /** Date patterns. */
  private static final Pattern[] PATTERNS = {
    Pattern.compile("(-?)([0-9]{4})" + ZONE),
    Pattern.compile("(-?)([0-9]{4})-([0-9]{2})" + ZONE),
    Pattern.compile("--([0-9]{2})" + ZONE),
    Pattern.compile("--([0-9]{2})-([0-9]{2})" + ZONE),
    Pattern.compile("---([0-9]{2})" + ZONE)
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
  DSim(final Date d, final Type t) {
    super(t, d);
    if(t != Type.YEA && t != Type.YMO) xc.setYear(UNDEF);
    if(t != Type.MON && t != Type.YMO && t != Type.MDA) xc.setMonth(UNDEF);
    if(t != Type.DAY && t != Type.MDA) xc.setDay(UNDEF);
    xc.setTime(UNDEF, UNDEF, UNDEF);
    xc.setMillisecond(UNDEF);
  }

  /**
   * Constructor.
   * @param d date
   * @param t data type
   * @param ii input info
   * @throws QueryException query exception
   */
  DSim(final byte[] d, final Type t, final InputInfo ii) throws QueryException {
    super(t, d, EXAMPLES[type(t)], ii);

    final int i = type(t);
    final Matcher mt = PATTERNS[i].matcher(Token.string(d).trim());
    if(!mt.matches()) dateErr(d, EXAMPLES[i], ii);
    zone(mt, ZONES[i], d, ii);

    if(t == Type.MDA) {
      final int m = xc.getMonth() - 1;
      if(xc.getDay() > DAYS[m] + (m == 1 ? 1 : 0))
        Err.or(ii, DATERANGE, type, d);
    }
  }

  /**
   * Returns the offset for the specified type.
   * @param type type
   * @return offset
   */
  private static int type(final Type type) {
    for(int t = 0; t < TYPES.length; ++t) if(TYPES[t] == type) return t;
    Util.notexpected();
    return -1;
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    Err.diff(ii, it, this);
    return 0;
  }
}
