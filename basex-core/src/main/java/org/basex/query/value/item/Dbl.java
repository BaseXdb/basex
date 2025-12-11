package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.fn.FnRound.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Double item ({@code xs:double}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Dbl extends ANum {
  /** Value "NaN". */
  public static final Dbl NAN = new Dbl(Double.NaN);
  /** Value "0". */
  public static final Dbl ZERO = new Dbl(0);
  /** Value "-0". */
  public static final Dbl NEGATIVE_ZERO = new Dbl(-0e0);
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
    if(!Double.isFinite(value)) throw valueError(AtomType.DECIMAL, string(), ii);
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
  public Dbl round(final int prec, final RoundMode mode) {
    if(value == 0 || !Double.isFinite(value)) return this;
    final double d = Dec.round(new BigDecimal(value), prec, mode).doubleValue();
    return d == 0 && Double.doubleToRawLongBits(value) < 0 ? NEGATIVE_ZERO :
      d == value ? this : Dbl.get(d);
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return item.type.instanceOf(AtomType.DECIMAL) ?
      -item.compare(this, coll, transitive, ii) :
      compare(value, item.dbl(ii), transitive);
  }

  /**
   * Compares two double values (identical for floats).
   * @param d1 first double value
   * @param d2 second double value
   * @param transitive transitive comparison
   * @return difference
   */
  static int compare(final double d1, final double d2, final boolean transitive) {
    final boolean n1 = Double.isNaN(d1), n2 = Double.isNaN(d2);
    return n1 || n2 ? transitive ? n1 == n2 ? 0 : n1 ? -1 : 1 : NAN_DUMMY :
      d1 < d2 ? -1 : d1 > d2 ? 1 : 0;
  }

  @Override
  public Double toJava() {
    return value;
  }

  @Override
  public int hashCode() {
    final double v = value;
    final int i = (int) v;
    return v != i && Double.isFinite(v) ? super.hashCode() : i;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Dbl dbl && Double.compare(value, dbl.value) == 0;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the given token into a double value.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return double value
   * @throws QueryException query exception
   */
  public static double parse(final byte[] value, final InputInfo info) throws QueryException {
    final double d = Token.toDouble(value);
    if(!Double.isNaN(d)) return d;

    final byte[] v = Token.trim(value);
    if(Token.eq(v, Token.NAN)) return Double.NaN;
    if(Token.eq(v, Token.POSITIVE_INF)) return Double.POSITIVE_INFINITY;
    if(Token.eq(v, Token.NEGATIVE_INF)) return Double.NEGATIVE_INFINITY;
    throw AtomType.DOUBLE.castError(value, info);
  }

  /**
   * Returns a JSON-compliant string representation of a double value.
   * @param value double value
   * @return string
   */
  public static byte[] string(final double value) {
    final byte[] token = Token.fastToken(value);
    if(token != null) return token;

    final BigDecimal bd = BigDecimal.valueOf(value).stripTrailingZeros();
    final double abs = Math.abs(value);
    return value == 0 && Double.doubleToRawLongBits(value) < 0 ? Token.NEGATIVE_ZERO :
      abs >= 1e-6 && abs < 1e21 ? Token.token(bd.toPlainString()) :
        Token.token(bd.toString().replace('E', 'e').replace("e+", "e"));
  }
}
