package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.datatype.Duration;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Duration item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class Dur extends Item {
  /** Seconds per day. */
  protected static final long DAYSECONDS = 86400;
  /** Date pattern. */
  private static final Pattern DUR = Pattern.compile(
      "(-?)P(([0-9]+)Y)?(([0-9]+)M)?(([0-9]+)D)?" +
      "(T(([0-9]+)H)?(([0-9]+)M)?(([0-9]+(\\.[0-9]+)?)?S)?)?");

  /** Number of months. */
  protected int mon;
  /** Number of seconds. */
  protected BigDecimal sc;

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
  protected Dur(final Type t) {
    super(t);
  }

  /**
   * Constructor.
   * @param d duration
   */
  protected Dur(final Dur d) {
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
    sc = d.sc == null ? BigDecimal.valueOf(0) : d.sc;
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   * @param ii input info
   * @throws QueryException query exception
   */
  private Dur(final byte[] v, final Type t, final InputInfo ii)
      throws QueryException {
    this(t);

    final String val = Token.string(v).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T"))
      dateErr(v, XDURR, ii);
    final int y = mt.group(2) != null ? toInt(mt.group(3)) : 0;
    final int m = mt.group(4) != null ? toInt(mt.group(5)) : 0;
    final long d = mt.group(6) != null ? toInt(mt.group(7)) : 0;
    final long h = mt.group(9) != null ? toInt(mt.group(10)) : 0;
    final long n = mt.group(11) != null ? toInt(mt.group(12)) : 0;
    final double s = mt.group(13) != null ? toDouble(token(mt.group(14))) : 0;

    mon = y * 12 + m;
    sc = BigDecimal.valueOf(d * DAYSECONDS + h * 3600 + n * 60);
    sc = sc.add(BigDecimal.valueOf(s));
    if(!mt.group(1).isEmpty()) {
      mon = -mon;
      sc = sc.negate();
    }
  }

  /**
   * Returns the years.
   * @return year
   */
  public final int yea() {
    return mon / 12;
  }

  /**
   * Returns the months.
   * @return year
   */
  public final int mon() {
    return mon % 12;
  }

  /**
   * Returns the days.
   * @return day
   */
  public final long day() {
    return sc.longValue() / DAYSECONDS;
  }

  /**
   * Returns the time.
   * @return time
   */
  private long tim() {
    return sc.longValue() % DAYSECONDS;
  }

  /**
   * Returns the hours.
   * @return day
   */
  public final long hou() {
    return tim() / 3600;
  }

  /**
   * Returns the minutes.
   * @return day
   */
  public final long min() {
    return tim() % 3600 / 60;
  }

  /**
   * Returns the seconds.
   * @return day
   */
  public final BigDecimal sec() {
    return sc.remainder(BigDecimal.valueOf(60));
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    if(mon < 0 || sc.signum() < 0) tb.add('-');
    tb.add('P');
    if(yea() != 0) { tb.addLong(Math.abs(yea())); tb.add('Y'); }
    if(mon() != 0) { tb.addLong(Math.abs(mon())); tb.add('M'); }
    if(day() != 0) { tb.addLong(Math.abs(day())); tb.add('D'); }
    if(sc.remainder(BigDecimal.valueOf(DAYSECONDS)).signum() != 0) {
      tb.add('T');
      if(hou() != 0) { tb.addLong(Math.abs(hou())); tb.add('H'); }
      if(min() != 0) { tb.addLong(Math.abs(min())); tb.add('M'); }
      if(sec().signum() != 0) { tb.add(sc()); tb.add('S'); }
    }
    if(mon == 0 && sc.signum() == 0) tb.add("T0S");
    return tb.finish();
  }

  /**
   * Returns the seconds in a token.
   * @return seconds
   */
  protected final byte[] sc() {
    return chopNumber(token(sec().abs().toPlainString()));
  }

  @Override
  public final boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    final Dur d = (Dur) (!it.type.isDuration() ? type.e(it, null, ii) : it);
    final double s1 = sc == null ? 0 : sc.doubleValue();
    final double s2 = d.sc == null ? 0 : d.sc.doubleValue();
    return mon == d.mon && s1 == s2;
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    throw Err.diff(ii, it, this);
  }

  @Override
  public final Duration toJava() {
    return Date.df.newDuration(Token.string(string(null)));
  }

  @Override
  public final int hash(final InputInfo ii) {
    return (int) (31 * mon + (sc == null ? 0 : sc.doubleValue()));
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", string(null));
  }
}
