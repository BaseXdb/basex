package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * DayTime Duration item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DTd extends Dur {
  /** DayTime pattern. */
  private static final Pattern DUR = Pattern.compile("(-?)P(([0-9]+)D)?" +
    "(T(([0-9]+)H)?(([0-9]+)M)?(([0-9]+)(\\.[0-9]+)?S)?)?");

  /**
   * Constructor.
   * @param it duration item
   */
  public DTd(final Dur it) {
    super(Type.DTD);
    sec = it.sec;
    mil = it.mil;
    minus  = it.minus && (it.sec != 0 || it.mil != 0);
  }

  /**
   * Timezone constructor.
   * @param shift shift value
   */
  public DTd(final int shift) {
    super(Type.DTD);
    zone = true;
    sec = Math.abs(shift * 60);
    minus = shift < 0;
  }

  /**
   * Constructor.
   * @param it duration item
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   */
  public DTd(final DTd it, final DTd a, final boolean p) {
    this(it);

    if(minus) { sec = -sec; mil = -mil; }
    sec += p ^ a.minus ? a.sec : -a.sec;
    mil += p ^ a.minus ? a.mil : -a.mil;
    minus = sec < 0 || mil < 0;
    sec = Math.abs(sec);
    mil = Math.abs(mil);
  }

  /**
   * Constructor.
   * @param it duration item
   * @param f factor
   * @param m multiplication flag
   * @throws XQException evaluation exception
   */
  public DTd(final Dur it, final double f, final boolean m)
      throws XQException {
    this(it);

    if(f != f) Err.or(DATECALC, info(), f);
    if(m ? f == 1 / 0d || f == -1 / 0d : f == 0) Err.or(DATEZERO, info(), f);
    final double d = m ? (sec + mil) * f : (sec + mil) / f;
    sec = Math.abs((long) d);
    mil = Math.abs(Math.floor((Math.abs(d) - sec) * 1000000 + .5) / 1000000);
    minus = d < 0 && (sec != 0 || mil != 0);
  }

  /**
   * Constructor.
   * @param it date item
   * @param sub date to be subtracted
   */
  public DTd(final Date it, final Date sub) {
    super(Type.DTD);

    sec = (sub.zshift - it.zshift) * 60;

    minus = false;
    if(it.type == Type.DAT || it.type == Type.DTM) {
      minus = it.mon < sub.mon || it.mon == sub.mon && it.sec < sub.sec;
      final long s = (minus ? days(it, sub) : days(sub, it)) * DAYSECONDS;
      sec = minus ? sec - s : sec + s;
    }
    if(it.type == Type.TIM || it.type == Type.DTM) {
      sec += it.sec % DAYSECONDS - sub.sec % DAYSECONDS;
      mil = it.mil - sub.mil;
      minus |= sec < 0 || sec == 0 && mil < 0;
      if(sec > 0 && mil < 0) {
        sec--;
        mil = 1 + mil;
      }
    }
    mon = Math.abs(mon);
    sec = Math.abs(sec);
    mil = Math.abs(mil);
  }

  /**
   * Constructor.
   * @param v value
   * @throws XQException evaluation exception
   */
  public DTd(final byte[] v) throws XQException {
    super(Type.DTD);

    final String val = Token.string(v).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T"))
      Err.date(type, XPDTD);

    final int d = mt.group(2) != null ? Token.toInt(mt.group(3)) : 0;
    final int h = mt.group(5) != null ? Token.toInt(mt.group(6)) : 0;
    final int n = mt.group(7) != null ? Token.toInt(mt.group(8)) : 0;
    final int s = mt.group(9) != null ? Token.toInt(mt.group(10)) : 0;

    sec = d * (long) DAYSECONDS + h * 3600 + n * 60 + s;
    mil = mt.group(11) != null ? Double.parseDouble(mt.group(11)) : 0;
    minus = mt.group(1).length() != 0 && (sec != 0 || mil != 0);
  }

  /**
   * Returns the number of days after the specified date.
   * @param s start date
   * @param e end date
   * @return days
   */
  private long days(final Date s, final Date e) {
    int ys = s.mon / 12;
    int ms = s.mon % 12;
    int ds = (int) (s.sec / DAYSECONDS);
    final int ye = e.mon / 12;
    final int me = e.mon % 12;
    final int de = (int) (e.sec / DAYSECONDS);
    long days = 0;
    while(ys < ye || ms < me || ds < de) {
      ds++;
      if(ds > dpm(ys, ms)) {
        ds = 0;
        ms++;
      }
      if(ms == 12) {
        ms = 0;
        ys++;
      }
      days++;
    }
    return days;
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    if(minus) tb.add('-');
    tb.add('P');
    final int d = (int) (sec / DAYSECONDS);
    final int t = (int) (sec % DAYSECONDS);
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
    if(sec == 0 && mil == 0) tb.add(Token.token("T0S"));
    return tb.finish();
  }

  @Override
  public int diff(final Item it) throws XQException {
    if(it.type != type) Err.cmp(it, this);
    return diff((Date) it);
  }
}
