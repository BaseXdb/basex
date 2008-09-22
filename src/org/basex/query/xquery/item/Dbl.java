package org.basex.query.xquery.item;

import java.math.BigDecimal;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * Double item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Dbl extends Num {
  /** Zero value. */
  public static final Dbl ZERO = new Dbl(0);
  /** Zero value. */
  public static final Dbl ONE = new Dbl(1);
  /** Invalid value. */
  public static final Dbl NAN = new Dbl(Double.NaN);
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
    return d == 0 && d == 1 / 0.0 ? ZERO : d == 1 ? ONE : d != d ? NAN :
      new Dbl(d);
  }

  /**
   * Returns an iterator.
   * @param v double value
   * @return item
   */
  public static Iter iter(final double v) {
    return new Iter() {
      boolean more;
      @Override
      public Item next() { return (more ^= true) ? get(v) : null; }
      @Override
      public String toString() { return Token.string(Token.token(v)); }
    };
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   * @throws XQException evaluation exception
   */
  public static Dbl get(final byte[] v) throws XQException {
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
  public BigDecimal dec() throws XQException {
    return Dec.parse(val);
  }

  @Override
  public boolean eq(final Item it) throws XQException {
    return val == it.dbl();
  }

  @Override
  public int diff(final Item it) throws XQException {
    final double n = it.dbl();
    if(n != n || val != val) return Integer.MIN_VALUE;
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
   * @throws XQException possible converting exception
   */
  public static double parse(final byte[] val) throws XQException {
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
