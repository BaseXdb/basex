package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.math.*;
import java.util.regex.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Duration item ({@code xs:duration}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class Dur extends ADateDur {
  /** Pattern for one or more digits. */
  static final String DP = "(\\d+)";

  /** Date pattern. */
  private static final Pattern DUR = Pattern.compile("(-?)P(" + DP + "Y)?(" + DP +
      "M)?(" + DP + "D)?(T(" + DP + "H)?(" + DP + "M)?((\\d+|\\d*\\.\\d+)?S)?)?");

  /** Number of months. */
  long mon;

  /**
   * Constructor.
   * @param value value
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dur(final byte[] value, final InputInfo ii) throws QueryException {
    this(value, AtomType.DUR, ii);
  }

  /**
   * Constructor.
   * @param type item type
   */
  Dur(final Type type) {
    super(type);
  }

  /**
   * Constructor.
   * @param dur duration
   */
  public Dur(final Dur dur) {
    this(dur, AtomType.DUR);
  }

  /**
   * Constructor.
   * @param dur duration
   * @param type item type
   */
  private Dur(final Dur dur, final Type type) {
    this(type);
    mon = dur.mon;
    sec = dur.sec == null ? BigDecimal.ZERO : dur.sec;
  }

  /**
   * Constructor.
   * @param value value
   * @param type item type
   * @param ii input info
   * @throws QueryException query exception
   */
  private Dur(final byte[] value, final Type type, final InputInfo ii) throws QueryException {
    this(type);
    final String val = Token.string(value).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T")) throw dateError(value, XDURR, ii);
    yearMonth(value, mt, ii);
    dayTime(value, mt, 6, ii);
  }

  /**
   * Initializes the yearMonth component.
   * @param vl value
   * @param mt matcher
   * @param ii input info
   * @throws QueryException query exception
   */
  void yearMonth(final byte[] vl, final Matcher mt, final InputInfo ii) throws QueryException {
    final long y = mt.group(2) != null ? toLong(mt.group(3), true, ii) : 0;
    final long m = mt.group(4) != null ? toLong(mt.group(5), true, ii) : 0;
    mon = y * 12 + m;
    double v = y * 12d + m;
    if(!mt.group(1).isEmpty()) {
      mon = -mon;
      v = -v;
    }
    if(v <= Long.MIN_VALUE || v >= Long.MAX_VALUE) throw DURRANGE_X_X.get(ii, type, vl);
  }

  /**
   * Initializes the dayTime component.
   * @param vl value
   * @param mt matcher
   * @param p first matching position
   * @param ii input info
   * @throws QueryException query exception
   */
  void dayTime(final byte[] vl, final Matcher mt, final int p, final InputInfo ii)
      throws QueryException {

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
    if(v <= Long.MIN_VALUE || v >= Long.MAX_VALUE) throw DURRANGE_X_X.get(ii, type, vl);
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
  final void date(final TokenBuilder tb) {
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
  final void time(final TokenBuilder tb) {
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
  public final boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final Dur d = (Dur) (it instanceof Dur ? it : type.cast(it, null, null, ii));
    final BigDecimal s1 = sec == null ? BigDecimal.ZERO : sec;
    final BigDecimal s2 = d.sec == null ? BigDecimal.ZERO : d.sec;
    return mon == d.mon && s1.compareTo(s2) == 0;
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    throw diffError(ii, it, this);
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
