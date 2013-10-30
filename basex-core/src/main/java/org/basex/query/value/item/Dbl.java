package org.basex.query.value.item;

import static java.lang.Double.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Double item ({@code xs:double}).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Dbl extends ANum {
  /** Value "NaN". */
  public static final Dbl NAN = new Dbl(NaN);
  /** Value "0". */
  private static final Dbl ZERO = new Dbl(0);
  /** Value "1". */
  private static final Dbl ONE = new Dbl(1);
  /** Data. */
  private final double val;

  /**
   * Constructor.
   * @param v value
   */
  private Dbl(final double v) {
    super(AtomType.DBL);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param d value
   * @return instance
   */
  public static Dbl get(final double d) {
    return d == 0 && doubleToRawLongBits(d) == 0 ? ZERO : d == 1 ? ONE :
      isNaN(d) ? NAN : new Dbl(d);
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Dbl get(final byte[] v, final InputInfo ii) throws QueryException {
    return get(parse(v, ii));
  }

  @Override
  public byte[] string() {
    return Token.token(val);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !isNaN(val) && val != 0;
  }

  @Override
  public long itr() {
    return (long) val;
  }

  @Override
  public float flt() {
    return (float) val;
  }

  @Override
  public double dbl() {
    return val;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    return Dec.parse(val, ii);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return val == it.dbl(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final double n = it.dbl(ii);
    if(isNaN(n) || isNaN(val)) return UNDEF;
    return val < n ? -1 : val > n ? 1 : 0;
  }

  @Override
  public Double toJava() {
    return val;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Dbl && val == ((Dbl) cmp).val || this == NAN && cmp == NAN;
  }

  /**
   * Converts the given token into a double value.
   * @param val value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static double parse(final byte[] val, final InputInfo ii) throws QueryException {
    try {
      return parseDouble(Token.string(val));
    } catch(final NumberFormatException ex) {
      if(Token.eq(Token.trim(val), Token.INF)) return POSITIVE_INFINITY;
      if(Token.eq(Token.trim(val), Token.NINF)) return NEGATIVE_INFINITY;
      throw ZERO.castErr(val, ii);
    }
  }
}
