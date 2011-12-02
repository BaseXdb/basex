package org.basex.query.item;

import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import static java.lang.Double.isNaN;

/**
 * Double item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Dbl extends Num {
  /** Invalid value. */
  public static final Dbl NAN = new Dbl(Double.NaN);
  /** Zero value. */
  private static final Dbl ZERO = new Dbl(0);
  /** Zero value. */
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
    return d == 0 && d == 1 / 0d ? ZERO : d == 1 ? ONE : isNaN(d) ? NAN :
      new Dbl(d);
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Dbl get(final byte[] v, final InputInfo ii)
      throws QueryException {
    return get(parse(v, ii));
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return Token.token(val);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !isNaN(val) && val != 0;
  }

  @Override
  public long itr(final InputInfo ii) {
    return (long) val;
  }

  @Override
  public float flt(final InputInfo ii) {
    return (float) val;
  }

  @Override
  public double dbl(final InputInfo ii) {
    return val;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    return Dec.parse(val, ii);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return val == it.dbl(ii);
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
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
    return cmp instanceof Dbl && val == ((Dbl) cmp).val ||
      this == NAN && cmp == NAN;
  }

  /**
   * Converts the given token into a double value.
   * @param val value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  static double parse(final byte[] val, final InputInfo ii)
      throws QueryException {

    try {
      return Double.parseDouble(Token.string(val));
    } catch(final NumberFormatException ex) {
      if(Token.eq(Token.trim(val), Token.INF)) return Double.POSITIVE_INFINITY;
      if(Token.eq(Token.trim(val), Token.NINF)) return Double.NEGATIVE_INFINITY;
      throw ZERO.castErr(val, ii);
    }
  }
}
