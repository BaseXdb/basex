package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.value.item.Dec.*;

import java.math.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Duration item ({@code xs:duration}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class Dur extends ADateDur {
  /** Number of months. */
  long months;
  /** Seconds and fractional seconds, within the range of a long value. */
  BigDecimal seconds;

  /**
   * Constructor.
   * @param value value
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dur(final byte[] value, final InputInfo info) throws QueryException {
    this(value, BasicType.DURATION, info);
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
    this(BasicType.DURATION);
    months = dur.months;
    seconds = dur.seconds;
  }

  /**
   * Constructor for multiplying or dividing a duration by a number.
   * @param dur duration item
   * @param factor factor
   * @param mult multiplication/division flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dur(final Dur dur, final double factor, final boolean mult, final InputInfo info)
      throws QueryException {
    this(BasicType.DURATION);
    checkFactor(factor, mult, type, info);
    months = scaleMonths(dur.months, factor, mult, info);
    seconds = scaleSeconds(dur.seconds, factor, mult);
    checkSeconds(info);
  }

  /**
   * Constructor.
   * @param value value
   * @param type item type
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private Dur(final byte[] value, final Type type, final InputInfo info) throws QueryException {
    this(type);
    final DateScanner ds = new DateScanner(value);
    if(!ds.duration(0, 3, true)) throw dateError(value, XDURR, info);
    yearMonth(value, ds, info);
    dayTime(value, ds, info);
  }

  /**
   * Initializes the yearMonth component.
   * @param value value
   * @param ds date scanner
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void yearMonth(final byte[] value, final DateScanner ds, final InputInfo info)
      throws QueryException {
    final long y = component(ds, 0, info), m = component(ds, 1, info);
    if(y > (Long.MAX_VALUE - m) / 12) throw DURRANGE_X_X.get(info, type, value);
    months = y * 12 + m;
    if(ds.neg) months = -months;
  }

  /**
   * Initializes the dayTime component.
   * @param value value
   * @param ds date scanner
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void dayTime(final byte[] value, final DateScanner ds, final InputInfo info)
      throws QueryException {
    final long d = component(ds, 2, info), h = component(ds, 3, info), m = component(ds, 4, info);
    final int s = ds.starts[5];
    final BigDecimal sc = s != -1 ? ds.decimal(s, ds.ends[5]) : BigDecimal.ZERO;
    try {
      // all components are non-negative: an overflow exceeds the supported range
      final long dhm = Math.addExact(Math.addExact(Math.multiplyExact(d, 86400),
          Math.multiplyExact(h, 3600)), Math.multiplyExact(m, 60));
      seconds = sc.add(BigDecimal.valueOf(dhm));
    } catch(final ArithmeticException ex) {
      throw DURRANGE_X_X.get(info, type, value).cause(ex);
    }
    if(ds.neg) seconds = seconds.negate();
    if(!secondsInRange()) throw DURRANGE_X_X.get(info, type, value);
  }

  /**
   * Returns the value of an integer duration component.
   * @param ds date scanner
   * @param c component offset
   * @param info input info (can be {@code null})
   * @return value, or {@code 0} if the component is undefined
   * @throws QueryException query exception
   */
  private long component(final DateScanner ds, final int c, final InputInfo info)
      throws QueryException {
    final int s = ds.starts[c];
    return s == -1 ? 0 : toLong(ds, s, ds.ends[c], info);
  }

  @Override
  public final long yea() {
    return months / 12;
  }

  @Override
  public final long mon() {
    return months % 12;
  }

  @Override
  public final long day() {
    return seconds.longValue() / 86400;
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
  public final BigDecimal seconds() {
    final long i = seconds.longValue(), r = i % 60;
    return i == r ? seconds : seconds.scale() <= 0 ? BigDecimal.valueOf(r) :
      seconds.subtract(BigDecimal.valueOf(i - r));
  }

  /**
   * Checks if the seconds component is within the supported range.
   * @return result of check
   */
  final boolean secondsInRange() {
    return seconds.abs().compareTo(BD_MAXLONG) <= 0;
  }

  /**
   * Checks if the seconds are within the supported range.
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void checkSeconds(final InputInfo info) throws QueryException {
    if(!secondsInRange()) throw SECDURRANGE_X.get(info, seconds.doubleValue());
  }

  /**
   * Returns the time as milliseconds.
   * @param info input info
   * @return milliseconds
   * @throws QueryException query exception
   */
  public long ms(final InputInfo info) throws QueryException {
    final BigDecimal ms = seconds.multiply(Dec.BD_1000);
    if(ms.compareTo(Dec.BD_MAXLONG) > 0) throw INTRANGE_X.get(info, ms);
    return ms.longValue();
  }

  /**
   * Returns the time.
   * @return time
   */
  private long tim() {
    return seconds.longValue() % 86400;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    final int ss = seconds.signum();
    if(months < 0 || ss < 0) tb.add('-');
    date(tb);
    time(tb);
    if(months == 0 && ss == 0) tb.add("T0S");
    return tb.finish();
  }

  @Override
  public final boolean comparable(final Item item) {
    return item instanceof Dur;
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
    final long t = tim();
    final BigDecimal sc = seconds();
    if(t == 0 && sc.signum() == 0) return;
    tb.add('T');
    final long h = t / 3600;
    if(h != 0) { tb.addLong(Math.abs(h)); tb.add('H'); }
    final long m = t % 3600 / 60;
    if(m != 0) { tb.addLong(Math.abs(m)); tb.add('M'); }
    if(sc.signum() == 0) return;
    if(sc.scale() <= 0) tb.addLong(Math.abs(sc.longValue()));
    else tb.add(Token.chopNumber(Token.token(sc.abs().toPlainString())));
    tb.add('S');
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final QueryContext qc, final InputInfo ii) throws QueryException {
    final Dur dur = (Dur) (item instanceof Dur ? item : type.cast(item, qc, ii));
    final int m = Long.compare(months, dur.months);
    return m != 0 ? m : seconds.compareTo(dur.seconds);
  }

  @Override
  public final Duration toJava() {
    return ADate.DF.newDuration(Token.string(string(null)));
  }

  @Override
  public final int hashCode() {
    return (int) (31 * months + seconds.doubleValue());
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof final Dur dur && type.eq(dur.type) &&
        months == dur.months && seconds.equals(dur.seconds);
  }

  /**
   * Validates a duration arithmetic factor; raises an error for NaN or infinite/zero divisors.
   * @param factor factor
   * @param mult multiplication/division flag
   * @param type duration type
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  static void checkFactor(final double factor, final boolean mult, final Type type,
      final InputInfo info) throws QueryException {
    if(Double.isNaN(factor)) throw DATECALC_X_X.get(info, type, factor);
    if(mult ? Double.isInfinite(factor) : factor == 0) throw DATEZERO_X_X.get(info, type, factor);
  }

  /**
   * Scales a months component by a factor.
   * @param months months
   * @param factor factor
   * @param mult multiplication/division flag
   * @param info input info (can be {@code null})
   * @return scaled months
   * @throws QueryException query exception
   */
  static long scaleMonths(final long months, final double factor, final boolean mult,
      final InputInfo info) throws QueryException {
    final double d = mult ? months * factor : months / factor;
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw MONTHRANGE_X.get(info, d);
    return StrictMath.round(d);
  }

  /**
   * Scales a seconds component by a factor.
   * @param seconds seconds
   * @param factor factor
   * @param mult multiplication/division flag
   * @return scaled seconds
   */
  static BigDecimal scaleSeconds(final BigDecimal seconds, final double factor,
      final boolean mult) {
    if(mult ? factor == 0 : Double.isInfinite(factor)) return BigDecimal.ZERO;
    BigDecimal d = BigDecimal.valueOf(factor), result;
    try {
      result = mult ? seconds.multiply(d) : seconds.divide(d, MathContext.DECIMAL64);
    } catch(final ArithmeticException ignore) {
      d = BigDecimal.valueOf(1 / factor);
      result = mult ? seconds.divide(d, MathContext.DECIMAL64) : seconds.multiply(d);
    }
    return Math.abs(result.doubleValue()) < 1.0E-13 ? BigDecimal.ZERO : result;
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.constructor(type, string(null));
  }
}
