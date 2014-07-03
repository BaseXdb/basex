package org.basex.query.value.item;

import static java.lang.Double.*;
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
  public static final Dbl NAN = new Dbl(NaN);
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
    return value == 0 && doubleToRawLongBits(value) == 0 ? ZERO : value == 1 ? ONE :
      isNaN(value) ? NAN : new Dbl(value);
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
    return !isNaN(value) && value != 0;
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
    return Dec.parse(value, ii);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return value == it.dbl(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final double n = it.dbl(ii);
    if(isNaN(n) || isNaN(value)) return UNDEF;
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
      throw FUNCAST.get(ii, ZERO.type, chop(val));
    }
  }
}
