package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.math.*;
import java.util.regex.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Duration item ({@code xs:duration}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class Dur extends ADateDur {
  /** Pattern for one or more digits. */
  protected static final String DP = "(\\d+)";

  /** Date pattern. */
  private static final Pattern DUR = Pattern.compile("(-?)P(" + DP + "Y)?(" + DP +
      "M)?(" + DP + "D)?(T(" + DP + "H)?(" + DP + "M)?((\\d+|\\d*\\.\\d+)?S)?)?");

  /** Number of months. */
  long mon;

  /**
   * Constructor.
   * @param v value
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dur(final byte[] v, final InputInfo ii) throws QueryException {
    this(v, AtomType.DUR, ii);
  }

  /**
   * Constructor.
   * @param t data type
   */
  Dur(final Type t) {
    super(t);
  }

  /**
   * Constructor.
   * @param d duration
   */
  public Dur(final Dur d) {
    this(d, AtomType.DUR);
  }

  /**
   * Constructor.
   * @param d duration
   * @param t data type
   */
  private Dur(final Dur d, final Type t) {
    this(t);
    mon = d.mon;
    sec = d.sec == null ? BigDecimal.ZERO : d.sec;
  }

  /**
   * Constructor.
   * @param vl value
   * @param t data type
   * @param ii input info
   * @throws QueryException query exception
   */
  private Dur(final byte[] vl, final Type t, final InputInfo ii) throws QueryException {
    this(t);

    final String val = Token.string(vl).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T")) dateErr(vl, XDURR, ii);
    yearMonth(vl, mt, ii);
    dayTime(vl, mt, 6, ii);
  }

  /**
   * Initializes the yearMonth component.
   * @param vl value
   * @param mt matcher
   * @param ii input info
   * @throws QueryException query exception
   */
  protected void yearMonth(final byte[] vl, final Matcher mt, final InputInfo ii)
      throws QueryException {

    final long y = mt.group(2) != null ? toLong(mt.group(3), true, ii) : 0;
    final long m = mt.group(4) != null ? toLong(mt.group(5), true, ii) : 0;
    mon = y * 12 + m;
    double v = y * 12d + m;
    if(!mt.group(1).isEmpty()) {
      mon = -mon;
      v = -v;
    }
    if(v <= Long.MIN_VALUE || v >= Long.MAX_VALUE) DURRANGE.thrw(ii, type, vl);
  }

  /**
   * Initializes the dayTime component.
   * @param vl value
   * @param mt matcher
   * @param p first matching position
   * @param ii input info
   * @throws QueryException query exception
   */
  protected void dayTime(final byte[] vl, final Matcher mt, final int p,
      final InputInfo ii) throws QueryException {

    final long d = mt.group(p) != null ? toLong(mt.group(p + 1), true, ii) : 0;
    final long h = mt.group(p + 3) != null ? toLong(mt.group(p + 4), true, ii) : 0;
    final long m = mt.group(p + 5) != null ? toLong(mt.group(p + 6), true, ii) : 0;
    final BigDecimal s = mt.group(p + 7) != null ? toDecimal(mt.group(p + 8), true, ii) :
      BigDecimal.ZERO;
    sec = s.add(BigDecimal.valueOf(d).multiply(DAYSECONDS)).
        add(BigDecimal.valueOf(h).multiply(BD3600)).
        add(BigDecimal.valueOf(m).multiply(BD60));
    if(!mt.group(1).isEmpty()) sec = sec.negate();
    final double v = sec.doubleValue();
    if(v <= Long.MIN_VALUE || v >= Long.MAX_VALUE) DURRANGE.thrw(ii, type, vl);
  }

  @Override
  public final long yea() {
    return mon / 12;
  }

  @Override
  public final long mon() {
    return mon % 12;
  }

  @Override
  public final long day() {
    return sec.divideToIntegralValue(DAYSECONDS).longValue();
  }

  @Override
  public final long hou() {
    return tim() / 3600;
  }

  @Override
  public final long min() {
    return tim() % 3600 / 60;
  }

  @Override
  public final BigDecimal sec() {
    return sec.remainder(BD60);
  }

  /**
   * Returns the time.
   * @return time
   */
  private long tim() {
    return sec.remainder(DAYSECONDS).longValue();
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    final int ss = sec.signum();
    if(mon < 0 || ss < 0) tb.add('-');
    date(tb);
    time(tb);
    if(mon == 0 && ss == 0) tb.add("T0S");
    return tb.finish();
  }

  /**
   * Adds the date to the specified token builder.
   * @param tb token builder
   */
  protected final void date(final TokenBuilder tb) {
    tb.add('P');
    final long y = yea();
    if(y != 0) { tb.addLong(Math.abs(y)); tb.add('Y'); }
    final long m = mon();
    if(m != 0) { tb.addLong(Math.abs(m)); tb.add('M'); }
    final long d = day();
    if(d != 0) { tb.addLong(Math.abs(d)); tb.add('D'); }
  }

  /**
   * Adds the time to the specified token builder.
   * @param tb token builder
   */
  protected final void time(final TokenBuilder tb) {
    if(sec.remainder(DAYSECONDS).signum() == 0) return;
    tb.add('T');
    final long h = hou();
    if(h != 0) { tb.addLong(Math.abs(h)); tb.add('H'); }
    final long m = min();
    if(m != 0) { tb.addLong(Math.abs(m)); tb.add('M'); }
    final BigDecimal sc = sec();
    if(sc.signum() == 0) return;
    tb.add(Token.chopNumber(Token.token(sc.abs().toPlainString()))).add('S');
  }

  @Override
  public final boolean eq(final InputInfo ii, final Item it) throws QueryException {
    final Dur d = (Dur) (it instanceof Dur ? it : type.cast(it, null, ii));
    final double s1 = sec == null ? 0 : sec.doubleValue();
    final double s2 = d.sec == null ? 0 : d.sec.doubleValue();
    return mon == d.mon && s1 == s2;
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    throw Err.diff(ii, it, this);
  }

  @Override
  public final Duration toJava() {
    return ADate.df.newDuration(Token.string(string(null)));
  }

  @Override
  public final int hash(final InputInfo ii) {
    return (int) (31 * mon + (sec == null ? 0 : sec.doubleValue()));
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", string(null));
  }
}
