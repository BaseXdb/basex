package org.basex.query.xquery.item;

import java.math.BigDecimal;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * Float item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Flt extends Num {
  /** Zero value. */
  public static final Flt ZERO = new Flt(0);
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
   * @param d value
   * @return instance
   */
  public static Flt get(final float d) {
    return d == 0 && d == 1 / 0.0 ? ZERO : new Flt(d);
  }

  /**
   * Returns an iterator.
   * @param v double value
   * @return item
   */
  public static Iter iter(final float v) {
    return new Iter() {
      boolean more;
      @Override
      public Item next() { return (more ^= true) ? get(v) : null; }
      @Override
      public String toString() { return Token.string(Token.token(v)); }
    };
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
  public BigDecimal dec() throws XQException {
    return Dec.parse(val);
  }

  @Override
  public boolean eq(final Item it) throws XQException {
    return it.type == Type.DBL ? it.eq(this) : dbl() == it.flt();
  }

  @Override
  public int diff(final Item it) throws XQException {
    final double n = it.flt();
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
   * @throws XQException possible converting exception
   */
  public static float parse(final byte[] val) throws XQException {
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
