package org.basex.query.xquery.item;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * Integer item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Itr extends Num {
  /** Decimal value. */
  protected long val;
  /** Zero value. */
  public static final Itr ZERO = new Itr(0);
  /** Constant values. */
  private static final Itr[] NUM = { ZERO, new Itr(1), new Itr(2),
    new Itr(3), new Itr(4), new Itr(5), new Itr(6), new Itr(7),
    new Itr(8), new Itr(9) };

  /**
   * Constructor.
   * @param v value
   */
  private Itr(final long v) {
    this(v, Type.ITR);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   */
  public Itr(final long v, final Type t) {
    super(t);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   */
  public static Itr get(final long v) {
    return v >= 0 && v <= 9 ? NUM[(int) v] : new Itr(v);
  }

  /**
   * Returns an iterator.
   * @param v value
   * @return item
   */
  public static Iter iter(final long v) {
    return new Iter() {
      boolean more;
      @Override
      public Item next() { return (more ^= true) ? get(v) : null; }
      @Override
      public String toString() { return Token.string(Token.token(v)); }
    };
  }

  @Override
  public final byte[] str() {
    return val == 0 ? Token.ZERO : Token.token(val);
  }

  @Override
  public final boolean bool() {
    return val != 0;
  }

  @Override
  public final long itr() {
    return val;
  }

  @Override
  public final float flt() {
    return val;
  }

  @Override
  public final double dbl() {
    return val;
  }

  @Override
  public BigDecimal dec() {
    return BigDecimal.valueOf(val);
  }

  @Override
  public final boolean eq(final Item it) throws XQException {
    return val == it.dbl();
  }

  @Override
  public final int diff(final Item it) throws XQException {
    final double n = it.dbl();
    return n != n ? Integer.MIN_VALUE : val < n ? -1 : val > n ? 1 : 0;
  }

  @Override
  public int hash() {
    return (int) val;
  }

  @Override
  public Object java() {
    switch(type) {
      case BYT:
        return (byte) val;
      case SHR:
      case UBY:
        return (short) val;
      case INT:
      case USH:
        return (int) val;
      case UIN:
        return val;
      default:
        return new BigInteger(Token.string(str()));
    }
  }

  /**
   * Converts the given item into a long value.
   * @param val value to be converted
   * @return long value
   * @throws XQException possible converting exception
   */
  static long parse(final byte[] val) throws XQException {
    int t = 0;
    final int l = val.length;
    while(t < l && val[t] >= 0 && val[t] <= ' ') t++;
    if(t == l) ZERO.castErr(val);
    boolean m = false;
    if(val[t] == '-' || val[t] == '+') m = val[t++] == '-';
    if(t == l) ZERO.castErr(val);
    long v = 0;
    for(; t < l; t++) {
      final byte c = val[t];
      if(c < '0' || c > '9') break;
      long w = (v << 3) + (v << 1) + c - '0';
      if(w < v) ZERO.castErr(val);
      v = w;
    }
    while(t < l && val[t] >= 0 && val[t] <= ' ') t++;
    if(t != l) ZERO.castErr(val);
    return m ? -v : v;
  }
}
