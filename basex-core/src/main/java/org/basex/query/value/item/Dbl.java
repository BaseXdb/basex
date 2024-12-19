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
 * @author BaseX Team 2005-24, BSD License
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
  public Dbl round(final int prec, final RoundMode mode) {
    if(value == 0 || Double.isNaN(value) || Double.isInfinite(value)) return this;
    final double d = Dec.round(new BigDecimal(value), prec, mode).doubleValue();
    return d == 0 && Double.doubleToRawLongBits(value) < 0 ? NEGATIVE_ZERO :
      d == value ? this : Dbl.get(d);
  }

  @Override
  public boolean equal(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    return value == item.dbl(ii);
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return transitive && item instanceof Dec ? -item.compare(this, coll, transitive, ii) :
      compare(value, item.dbl(ii), transitive);
  }

  /**
   * Compares two doubles.
   * @param d1 first double
   * @param d2 second double
   * @param transitive transitive comparison
   * @return result of comparison (-1, 0, 1)
   */
  static int compare(final double d1, final double d2, final boolean transitive) {
    final boolean nan = Double.isNaN(d1), dNan = Double.isNaN(d2);
    return nan || dNan ? transitive ? nan == dNan ? 0 : nan ? -1 : 1 : NAN_DUMMY :
      d1 < d2 ? -1 : d1 > d2 ? 1 : 0;
  }

  @Override
  public Double toJava() {
    return value;
  }

  @Override
  public int hash() {
    final double v = value;
    final int i = (int) v;
    return v == i || Double.isNaN(v) || Double.isInfinite(v) ? i : super.hash();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Dbl && Double.compare(value, ((Dbl) obj).value) == 0;
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
    if(Token.eq(v, Token.INF)) return Double.POSITIVE_INFINITY;
    if(Token.eq(v, Token.NEGATVE_INF)) return Double.NEGATIVE_INFINITY;

    throw AtomType.DOUBLE.castError(value, info);
  }
}
