package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * YearMonth Duration item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class YMd extends Dur {
  /** YearMonth pattern. */
  private static final Pattern DUR = Pattern.compile(
      "(-?)P(([0-9]+)Y)?(([0-9]+)M)?");

  /**
   * Constructor.
   * @param it duration item
   */
  public YMd(final Dur it) {
    super(Type.YMD);
    mon = it.mon;
    minus  = it.minus && it.mon != 0;
  }

  /**
   * Constructor.
   * @param it duration item
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   */
  public YMd(final YMd it, final YMd a, final boolean p) {
    this(it);

    if(minus) mon = -mon;
    mon += p ^ a.minus ? a.mon : -a.mon;
    minus = mon < 0;
    mon = Math.abs(mon);
  }

  /**
   * Constructor.
   * @param it duration item
   * @param f factor
   * @param m multiplication/division flag
   * @throws XQException evaluation exception
   */
  public YMd(final Dur it, final double f, final boolean m)
      throws XQException {
    this(it);

    if(f != f) Err.or(DATECALC, info(), f);
    if(m ? f == 1 / 0d || f == -1 / 0d : f == 0) Err.or(DATEZERO, info(), f);
    mon = (int) Math.round(m ? mon * f : mon / f);
    minus = mon < 0;
    mon = Math.abs(mon);
  }

  /**
   * Constructor.
   * @param v value
   * @throws XQException evaluation exception
   */
  public YMd(final byte[] v) throws XQException {
    super(Type.YMD);

    final String val = Token.string(v).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P")) Err.date(type, XPYMD);

    final int y = mt.group(2) != null ? Token.toInt(mt.group(3)) : 0;
    final int m = mt.group(4) != null ? Token.toInt(mt.group(5)) : 0;

    mon = y * 12 + m;
    minus = mt.group(1).length() != 0 && mon != 0;
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    if(minus) tb.add('-');
    tb.add('P');
    final int y = mon / 12;
    final int m = mon % 12;
    if(y != 0) { tb.add(y); tb.add('Y'); }
    if(m != 0) { tb.add(m); tb.add('M'); }
    if(mon == 0) tb.add(Token.token("0M"));
    return tb.finish();
  }

  @Override
  public int diff(final Item it) throws XQException {
    if(it.type != type) Err.cmp(it, this);
    return diff((Date) it);
  }
}
