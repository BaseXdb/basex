package org.basex.query.item;

import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.util.Token;

/**
 * Float item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Flt extends Item {
  /** Invalid value. */
  public static final Flt NAN = new Flt(Float.NaN);
  /** Zero value. */
  private static final Flt ZERO = new Flt(0);
  /** Data. */
  private final float val;

  /**
   * Constructor.
   * @param v value
   */
  private Flt(final float v) {
    super(Type.FLT);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param f value
   * @return instance
   */
  public static Flt get(final float f) {
    return f == 0 && f == 1 / 0d ? ZERO : f != f ? NAN : new Flt(f);
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
    return val;
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
    return it.type == Type.DBL ? it.eq(this) : dbl() == it.flt();
  }

  @Override
  public int diff(final Item it) throws QueryException {
    final double n = it.flt();
    if(n != n || val != val) return UNDEF;
    return val < n ? -1 : val > n ? 1 : 0;
  }

  @Override
  public Object java() {
    return val;
  }

  @Override
  public int hashCode() {
    return (int) val;
  }

  /**
   * Converts the given token into a double value.
   * @param val value to be converted
   * @return double value
   * @throws QueryException query exception
   */
  static float parse(final byte[] val) throws QueryException {
    try {
      return Float.parseFloat(Token.string(val));
    } catch(final NumberFormatException ex) {
      if(Token.eq(Token.trim(val), Token.INF)) return Float.POSITIVE_INFINITY;
      if(Token.eq(Token.trim(val), Token.NINF)) return Float.NEGATIVE_INFINITY;
      ZERO.castErr(val);
      return 0f;
    }
  }
}
