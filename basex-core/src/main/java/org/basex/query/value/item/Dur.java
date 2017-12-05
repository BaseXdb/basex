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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class Dur extends ADateDur {
  /** Pattern for one or more digits. */
  static final String DP = "(\\d+)";

  /** YearMonth pattern. */
  public static final Pattern YMD = Pattern.compile("(-?)P(" + DP + "Y)?(" + DP + "M)?");
  /** DayTime pattern. */
  public static final Pattern DTD = Pattern.compile(
      "(-?)P(" + DP + "D)?(T(" + DP + "H)?(" + DP + "M)?((\\d+(\\.\\d+)?)S)?)?");
  /** Duration pattern. */
  public static final Pattern DUR = Pattern.compile("(-?)P(" + DP + "Y)?(" + DP +
      "M)?(" + DP + "D)?(T(" + DP + "H)?(" + DP + "M)?((\\d+|\\d*\\.\\d+)?S)?)?");

  /** Number of months. */
  long mon;

  /**
   * Constructor.
   * @param value value
   * @param info input info
   * @throws QueryException query exception
   */
  public Dur(final byte[] value, final InputInfo info) throws QueryException {
    this(value, AtomType.DUR, info);
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
   * @param info input info
   * @throws QueryException query exception
   */
  private Dur(final byte[] value, final Type type, final InputInfo info) throws QueryException {
    this(type);
    final String val = Token.string(value).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T")) throw dateError(value, XDURR, info);
    yearMonth(value, mt, info);
    dayTime(value, mt, 6, info);
  }

  /**
   * Initializes the yearMonth component.
   * @param vl value
   * @param mt matcher
   * @param info input info
   * @throws QueryException query exception
   */
  void yearMonth(final byte[] vl, final Matcher mt, final InputInfo info) throws QueryException {
    final long y = mt.group(2) != null ? toLong(mt.group(3), true, info) : 0;
    final long m = mt.group(4) != null ? toLong(mt.group(5), true, info) : 0;
    mon = y * 12 + m;
    double v = y * 12.0d + m;
    if(!mt.group(1).isEmpty()) {
      mon = -mon;
      v = -v;
    }
    if(v <= Long.MIN_VALUE || v >= Long.MAX_VALUE) throw DURRANGE_X_X.get(info, type, vl);
  }

  /**
   * Initializes the dayTime component.
   * @param value value
   * @param match matcher
   * @param pos first matching position
   * @param info input info
   * @throws QueryException query exception
   */
  void dayTime(final byte[] value, final Matcher match, final int pos, final InputInfo info)
      throws QueryException {

    final long d = match.group(pos) != null ? toLong(match.group(pos + 1), true, info) : 0;
    final long h = match.group(pos + 3) != null ? toLong(match.group(pos + 4), true, info) : 0;
    final long m = match.group(pos + 5) != null ? toLong(match.group(pos + 6), true, info) : 0;
    final BigDecimal s = match.group(pos + 7) != null ?
      toDecimal(match.group(pos + 8), true, info) : BigDecimal.ZERO;
    sec = s.add(BigDecimal.valueOf(d).multiply(DAYSECONDS)).
        add(BigDecimal.valueOf(h).multiply(BD3600)).
        add(BigDecimal.valueOf(m).multiply(BD60));
    if(!match.group(1).isEmpty()) sec = sec.negate();
    final double v = sec.doubleValue();
    if(v <= Long.MIN_VALUE || v >= Long.MAX_VALUE) throw DURRANGE_X_X.get(info, type, value);
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
  public final long hour() {
    return tim() / 3600;
  }

  @Override
  public final long minute() {
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
  public byte[] string(final InputInfo info) {
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
    final long h = hour();
    if(h != 0) { tb.addLong(Math.abs(h)); tb.add('H'); }
    final long m = minute();
    if(m != 0) { tb.addLong(Math.abs(m)); tb.add('M'); }
    final BigDecimal sc = sec();
    if(sc.signum() == 0) return;
    tb.add(Token.chopNumber(Token.token(sc.abs().toPlainString()))).add('S');
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo info) throws QueryException {
    final Dur d = (Dur) (item instanceof Dur ? item : type.cast(item, null, null, info));
    final BigDecimal s1 = sec == null ? BigDecimal.ZERO : sec;
    final BigDecimal s2 = d.sec == null ? BigDecimal.ZERO : d.sec;
    return mon == d.mon && s1.compareTo(s2) == 0;
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    throw diffError(item, this, info);
  }

  @Override
  public final Duration toJava() {
    return ADate.DF.newDuration(Token.string(string(null)));
  }

  @Override
  public final int hash(final InputInfo info) {
    return (int) (31 * mon + (sec == null ? 0 : sec.doubleValue()));
  }

  @Override
  public final boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Dur)) return false;
    final Dur d = (Dur) obj;
    return type.eq(d.type) && mon == d.mon &&
        (sec == null ? d.sec == null : sec.compareTo(d.sec) == 0);
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", string(null));
  }
}
