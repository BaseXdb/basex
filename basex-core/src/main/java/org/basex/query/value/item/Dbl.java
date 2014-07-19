package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Double item ({@code xs:double}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Dbl extends ANum {
  /** Value "NaN". */
  public static final Dbl NAN = new Dbl(Double.NaN);
  /** Value "0". */
  private static final Dbl ZERO = new Dbl(0);
  /** Value "1". */
  private static final Dbl ONE = new Dbl(1);
  /** Data. */
  private final double value;

  /**
   * Constructor.
   * @param value value
   */
  private Dbl(final double value) {
    super(AtomType.DBL);
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
      throw valueError(ii, AtomType.DEC, Dbl.get(value));
    return BigDecimal.valueOf(value);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    return value == it.dbl(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    final double n = it.dbl(ii);
    if(Double.isNaN(n) || Double.isNaN(value)) return UNDEF;
    return value < n ? -1 : value > n ? 1 : 0;
  }

  @Override
  public Double toJava() {
    return value;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Dbl && value == ((Dbl) cmp).value || this == NAN && cmp == NAN;
  }

  /**
   * Converts the given token into a double value.
   * @param value value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static double parse(final byte[] value, final InputInfo ii) throws QueryException {
    final double d = Token.toDouble(value);
    if(d == d) return d;
    final byte[] v = Token.trim(value);
    if(Token.eq(v, Token.NAN)) return Double.NaN;
    if(Token.eq(v, Token.INF)) return Double.POSITIVE_INFINITY;
    if(Token.eq(v, Token.NINF)) return Double.NEGATIVE_INFINITY;
    throw funCastError(ii, ZERO.type, v);
  }
}
