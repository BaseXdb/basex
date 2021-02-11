package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Double item ({@code xs:double}).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Dbl extends ANum {
  /** Value "NaN". */
  public static final Dbl NAN = new Dbl(Double.NaN);
  /** Value "0". */
  public static final Dbl ZERO = new Dbl(0);
  /** Value "1". */
  public static final Dbl ONE = new Dbl(1);
  /** Data. */
  private final double value;

  /**
   * Constructor.
   * @param value value
   */
  private Dbl(final double value) {
    super(AtomType.DOUBLE);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Dbl get(final double value) {
    return value == 0 && Double.doubleToRawLongBits(value) == 0 ? ZERO : value == 1 ? ONE :
      Double.isNaN(value) ? NAN : new Dbl(value);
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Dbl get(final byte[] value, final InputInfo ii) throws QueryException {
    return get(parse(value, ii));
  }

  @Override
  public byte[] string() {
    return Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !Double.isNaN(value) && value != 0;
  }

  @Override
  public long itr() {
    return (long) value;
  }

  @Override
  public float flt() {
    return (float) value;
  }

  @Override
  public double dbl() {
    return value;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    if(Double.isNaN(value) || Double.isInfinite(value))
     throw valueError(AtomType.DECIMAL, string(), ii);
    return new BigDecimal(value);
  }

  @Override
  public Dbl abs() {
    return value > 0.0d || 1 / value > 0 ? this : get(-value);
  }

  @Override
  public Dbl ceiling() {
    final double d = Math.ceil(value);
    return d == value ? this : get(d);
  }

  @Override
  public Dbl floor() {
    final double d = Math.floor(value);
    return d == value ? this : get(d);
  }

  @Override
  public Dbl round(final int scale, final boolean even) {
    final double v = rnd(scale, even);
    return v == value ? this : get(v);
  }

  /**
   * Returns a rounded value.
   * @param s scale
   * @param e half-to-even flag
   * @return result
   */
  private double rnd(final int s, final boolean e) {
    double v = value;
    if(v == 0 || Double.isNaN(v) || Double.isInfinite(v) || s > 322) return v;
    if(s < -308) return 0;
    if(!e && s == 0) {
      if(v >= -0.5 && v < 0.0) return -0.0;
      if(v > Long.MIN_VALUE && v < Long.MAX_VALUE) return Math.round(v);
    }

    final boolean n = v < 0;
    final double f = StrictMath.pow(10, s + 1);
    v = (n ? -v : v) * f;
    if(Double.isInfinite(v)) {
      final RoundingMode m = e ? RoundingMode.HALF_EVEN : RoundingMode.HALF_UP;
      v = new BigDecimal(value).setScale(s, m).doubleValue();
    } else {
      final double r = v % 10;
      v += r < 5 ? -r : (e ? r > 5 : r >= 5) ? 10 - r : e ? v % 20 == 15 ? 5 : -5 : 0;
      v /= f;
      if(n) v = -v;
    }
    return v;
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return value == item.dbl(ii);
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    // cannot be replaced by Double.compare (different semantics)
    final double n = item.dbl(ii);
    return Double.isNaN(n) || Double.isNaN(value) ? UNDEF : value < n ? -1 : value > n ? 1 : 0;
  }

  @Override
  public Double toJava() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Dbl && value == ((Dbl) obj).value;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the given token into a double value.
   * @param value value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static double parse(final byte[] value, final InputInfo ii) throws QueryException {
    final double d = Token.toDouble(value);
    if(!Double.isNaN(d)) return d;

    final byte[] v = Token.trim(value);
    if(Token.eq(v, Token.NAN)) return Double.NaN;
    if(Token.eq(v, Token.INF)) return Double.POSITIVE_INFINITY;
    if(Token.eq(v, Token.NEGATVE_INF)) return Double.NEGATIVE_INFINITY;

    throw AtomType.DOUBLE.castError(value, ii);
  }
}
