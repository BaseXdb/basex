package org.basex.query.item;

import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.util.Token;

/**
 * Double item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Dbl extends Item {
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
    super(Type.DBL);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param d value
   * @return instance
   */
  public static Dbl get(final double d) {
    return d == 0 && d == 1 / 0d ? ZERO : d == 1 ? ONE : d != d ? NAN :
      new Dbl(d);
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   * @throws QueryException evaluation exception
   */
  public static Dbl get(final byte[] v) throws QueryException {
    return get(parse(v));
  }

  @Override
  public byte[] str() {
    return Token.token(val);
  }

  @Override
  public boolean bool() {
    return val == val && val != 0;
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
  public BigDecimal dec() throws QueryException {
    return Dec.parse(val);
  }

  @Override
  public boolean eq(final Item it) throws QueryException {
    return val == it.dbl();
  }

  @Override
  public int diff(final Item it) throws QueryException {
    final double n = it.dbl();
    if(n != n || val != val) return UNDEF;
    return val < n ? -1 : val > n ? 1 : 0;
  }

  @Override
  public int hash() {
    return (int) val;
  }

  @Override
  public Object java() {
    return val;
  }

  /**
   * Converts the given token into a double value.
   * @param val value to be converted
   * @return double value
   * @throws QueryException possible converting exception
   */
  static double parse(final byte[] val) throws QueryException {
    try {
      return Double.parseDouble(Token.string(val));
    } catch(final NumberFormatException ex) {
      if(Token.eq(Token.trim(val), Token.INF)) return Double.POSITIVE_INFINITY;
      if(Token.eq(Token.trim(val), Token.NINF)) return Double.NEGATIVE_INFINITY;
      ZERO.castErr(val);
      return 0;
    }
  }
}
