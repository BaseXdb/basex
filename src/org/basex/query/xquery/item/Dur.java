package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Duration item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Dur extends Date {
  /** Seconds per day. */
  protected static final long MONTHSECONDS = 2678400;
  /** Date pattern. */
  private static final Pattern DUR = Pattern.compile(
      "(-?)P(([0-9]+)Y)?(([0-9]+)M)?(([0-9]+)D)?" +
      "(T(([0-9]+)H)?(([0-9]+)M)?(([0-9]+)(\\.[0-9]+)?S)?)?");

  /**
   * Constructor.
   * @param t data type
   */
  protected Dur(final Type t) {
    super(t);
  }

  /**
   * Constructor.
   * @param d duration
   */
  public Dur(final Dur d) {
    this(d, Type.DUR);
  }

  /**
   * Constructor.
   * @param d duration
   * @param t data type
   */
  public Dur(final Dur d, final Type t) {
    super(t);
    mon = d.mon;
    sec = d.sec;
    minus  = d.minus;
    mil = d.mil;
  }

  /**
   * Constructor.
   * @param v value
   * @throws XQException evaluation exception
   */
  public Dur(final byte[] v) throws XQException {
    this(v, Type.DUR);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   * @throws XQException evaluation exception
   */
  public Dur(final byte[] v, final Type t) throws XQException {
    super(t);

    final String val = Token.string(v).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T"))
      Err.date(type, XPDURR);
    final int y = mt.group(2) != null ? Token.toInt(mt.group(3)) : 0;
    final int m = mt.group(4) != null ? Token.toInt(mt.group(5)) : 0;
    final int d = mt.group(6) != null ? Token.toInt(mt.group(7)) : 0;
    final int h = mt.group(9) != null ? Token.toInt(mt.group(10)) : 0;
    final int n = mt.group(11) != null ? Token.toInt(mt.group(12)) : 0;
    final int s = mt.group(13) != null ? Token.toInt(mt.group(14)) : 0;

    mon = y * 12 + m;
    sec = d * (long) DAYSECONDS + h * 3600 + n * 60 + s;
    mil = mt.group(15) != null ? Double.parseDouble(mt.group(15)) : 0;
    minus = mt.group(1).length() != 0 && (mon != 0 || sec != 0 ||
        mil != 0);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    if(minus) tb.add('-');
    tb.add('P');

    final int y = mon / 12;
    final int m = mon % 12;
    final int d = (int) (sec / DAYSECONDS);
    final int t = (int) (sec % DAYSECONDS);
    if(y != 0) { tb.add(y); tb.add('Y'); }
    if(m != 0) { tb.add(m); tb.add('M'); }
    if(d != 0) { tb.add(d); tb.add('D'); }
    if(t != 0 || mil != 0) {
      tb.add('T');
      final int h = t / 3600;
      if(h != 0) { tb.add(h); tb.add('H'); }
      final int n = t % 3600 / 60;
      if(n != 0) { tb.add(n); tb.add('M'); }
      final int s = t % 60;
      if(s != 0 || mil != 0) {
        tb.add(s != 0 ? s : 0);
        if(mil != 0) tb.add(Token.substring(Token.token(mil), 1));
        tb.add('S');
      }
    }
    if(mon == 0 && sec == 0 && mil == 0) tb.add(Token.token("T0S"));
    return tb.finish();
  }

  @Override
  public int diff(final Item it) throws XQException {
    Err.cmp(it, this);
    return 0;
  }
}
